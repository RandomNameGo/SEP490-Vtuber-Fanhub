package com.sep490.vtuber_fanhub.configurations;

import com.sep490.vtuber_fanhub.models.User;
import com.sep490.vtuber_fanhub.repositories.UserRepository;
import com.sep490.vtuber_fanhub.services.JWTService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;

import java.util.Map;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class WebSocketAuthInterceptor implements HandshakeInterceptor {

    private final JWTService jwtService;
    private final UserRepository userRepository;

    @Override
    public boolean beforeHandshake(ServerHttpRequest request, ServerHttpResponse response,
                                   WebSocketHandler wsHandler, Map<String, Object> attributes) throws Exception {
        // Extract token from WebSocket handshake headers
        String authHeader = null;
        if (request.getHeaders().getFirst("Authorization") != null) {
            authHeader = request.getHeaders().getFirst("Authorization");
        }

        // Also check query parameter as fallback (for SockJS)
        // SockJS encodes query params as "info<param>=<value>" (e.g., "infotoken=xxx")
        if (authHeader == null && request.getURI().getQuery() != null) {
            String query = request.getURI().getQuery();
            String[] params = query.split("&");
            for (String param : params) {
                if (param.startsWith("token=")) {
                    authHeader = "Bearer " + param.substring(6);
                    break;
                }
                // SockJS prefix: "infotoken=xxx"
                if (param.startsWith("infotoken=")) {
                    authHeader = "Bearer " + param.substring(10);
                    break;
                }
            }
        }

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            response.setStatusCode(org.springframework.http.HttpStatus.UNAUTHORIZED);
            return false;
        }

        String token = authHeader.substring(7);

        try {
            String username = jwtService.getUsernameFromToken(token);
            Optional<User> user = userRepository.findByUsernameAndIsActive(username);

            if (user.isEmpty()) {
                response.setStatusCode(org.springframework.http.HttpStatus.UNAUTHORIZED);
                return false;
            }

            // Store user in WebSocket session attributes for later use
            attributes.put("username", username);
            attributes.put("user", user.get());

            return true;
        } catch (Exception e) {
            response.setStatusCode(org.springframework.http.HttpStatus.UNAUTHORIZED);
            return false;
        }
    }

    @Override
    public void afterHandshake(ServerHttpRequest request, ServerHttpResponse response,
                               WebSocketHandler wsHandler, Exception exception) {
        // No action needed after handshake
    }
}
