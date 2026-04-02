package com.sep490.vtuber_fanhub.controllers;
import com.sep490.vtuber_fanhub.dto.requests.SendMessageRequest;
import com.sep490.vtuber_fanhub.dto.responses.APIResponse;
import com.sep490.vtuber_fanhub.dto.responses.MessageResponse;
import com.sep490.vtuber_fanhub.models.User;
import com.sep490.vtuber_fanhub.services.ChatMessageService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.annotation.SendToUser;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;

@Controller
@RequiredArgsConstructor
public class WebsocketSendMessageController {
    private final ChatMessageService chatMessageService;
    /*
     * Frontend sends message to: /app/chat/sendMessage
     * User is authenticated via WebSocket handshake interceptor
     * Response is sent to: /user/queue/reply
     */
    @MessageMapping("chat/sendMessage")
    @SendToUser("queue/reply")
    public ResponseEntity<?> sendMessage(
            @Payload SendMessageRequest request,
            SimpMessageHeaderAccessor headerAccessor
    ) {
        try {
            // Get authenticated user from Spring Security context (set by ChannelInterceptor)
            Authentication auth = (Authentication) headerAccessor.getUser();
            if (auth == null || !auth.isAuthenticated()) {
                throw new RuntimeException("User not authenticated");
            }

            User user = (User) auth.getPrincipal();
            String username = user.getUsername();

            // Return the message to be broadcast to all subscribers
            ResponseEntity<?> response = ResponseEntity.ok().body(APIResponse.<MessageResponse>builder()
                    .success(true)
                    .message("Message sent successfully")
                    .data(chatMessageService.sendMessage(request, username))
                    .build());

            return response;

        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Error while sending message: " + e.getMessage());
        }
    }
}
