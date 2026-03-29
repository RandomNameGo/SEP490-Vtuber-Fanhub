package com.sep490.vtuber_fanhub.controllers;
import com.sep490.vtuber_fanhub.dto.requests.SendMessageRequest;
import com.sep490.vtuber_fanhub.dto.responses.APIResponse;
import com.sep490.vtuber_fanhub.dto.responses.MessageResponse;
import com.sep490.vtuber_fanhub.services.ChatMessageService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.annotation.SendToUser;
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
            // Get username from session attributes (set by handshake interceptor)
            String username = (String) headerAccessor.getSessionAttributes().get("username");
            
            if (username == null) {
                throw new RuntimeException("User not authenticated");
            }

            // Return the message to be broadcast to all subscribers
            return ResponseEntity.ok().body(APIResponse.<MessageResponse>builder()
                    .success(true)
                    .message("Message sent successfully")
                    .data(chatMessageService.sendMessage(request, username))
                    .build());

        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Error while sending message: ");
        }
    }
}
