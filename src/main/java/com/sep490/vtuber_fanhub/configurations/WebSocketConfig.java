package com.sep490.vtuber_fanhub.configurations;

import com.sep490.vtuber_fanhub.models.User;
import com.sep490.vtuber_fanhub.repositories.UserRepository;
import com.sep490.vtuber_fanhub.services.JWTService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

import java.util.Optional;

@Configuration
@EnableWebSocketMessageBroker
@RequiredArgsConstructor
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    private final JWTService jwtService;
    private final UserRepository userRepository;

    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        // Enable a simple in-memory broker for messages sent to @SendTo destinations
        config.enableSimpleBroker("/topic", "/queue");

        // Prefix for messages bound for @MessageMapping methods
        config.setApplicationDestinationPrefixes("/app");

        // Prefix for user-specific destinations (required for @SendToUser)
        config.setUserDestinationPrefix("/user");

        // Set heartbeat for connection health monitoring
        config.setPreservePublishOrder(true);
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        // Endpoint that clients will connect to (with SockJS)
        registry.addEndpoint("/ws-chat")
                .setAllowedOriginPatterns("*")
                .withSockJS();

        // Also expose a native WebSocket endpoint (for clients that don't use SockJS)
        registry.addEndpoint("/ws-chat")
                .setAllowedOriginPatterns("*");
    }

    @Override
    public void configureClientInboundChannel(ChannelRegistration registration) {
        registration.interceptors(new ChannelInterceptor() {
            @Override
            public Message<?> preSend(Message<?> message, MessageChannel channel) {
                StompHeaderAccessor accessor =
                        MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);

                if (accessor != null) {
                    // Handle CONNECT frame - authenticate the user
                    if (StompCommand.CONNECT.equals(accessor.getCommand())) {
                        // 1. Try to get token from Authorization header
                        String token = accessor.getFirstNativeHeader("Authorization");

                        // 2. Fallback: Get token from query param (for SockJS)
                        if (token == null || token.isEmpty()) {
                            java.util.List<String> nativeHeaders = accessor.getNativeHeader("token");
                            if (nativeHeaders != null && !nativeHeaders.isEmpty()) {
                                token = nativeHeaders.get(0);
                            }
                        }

                        if (token != null) {
                            if (token.startsWith("Bearer ")) {
                                token = token.substring(7);
                            }

                            try {
                                String username = jwtService.getUsernameFromToken(token);
                                Optional<User> userOpt = userRepository.findByUsernameAndIsActive(username);

                                if (userOpt.isPresent()) {
                                    User user = userOpt.get();
                                    UsernamePasswordAuthenticationToken auth =
                                            new UsernamePasswordAuthenticationToken(user, null, java.util.Collections.emptyList());

                                    accessor.setUser(auth);
                                }
                            } catch (Exception e) {
                                System.err.println("✗ WebSocket Auth Failed: " + e.getMessage());
                                e.printStackTrace();
                            }
                        } else {
                            System.err.println("✗ No token provided");
                        }
                    }
                    
                    // Handle SUBSCRIBE frame - log subscription
                    if (StompCommand.SUBSCRIBE.equals(accessor.getCommand())) {
                    }
                }
                return message;
            }
        });
    }

}
