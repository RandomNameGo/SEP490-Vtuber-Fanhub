package com.sep490.vtuber_fanhub.controllers;
import com.sep490.vtuber_fanhub.dto.requests.SendMessageRequest;
import com.sep490.vtuber_fanhub.dto.responses.APIResponse;
import com.sep490.vtuber_fanhub.dto.responses.SendMessageResponse;
import com.sep490.vtuber_fanhub.services.ChatMessageService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;
@Controller
@RequiredArgsConstructor
public class WebsocketSendMessageController {
    private final ChatMessageService chatMessageService;
    /*
     * Frontend sends message to: /app/chat/sendMessage
     * Message is then broadcast to: /topic/chat/{sessionId}
     */
    @MessageMapping("/chat/sendMessage")
    @SendTo("/topic/chat")
    public ResponseEntity<?> sendMessage(
            @Payload SendMessageRequest request
    ) {
        try {

            // Return the message to be broadcast to all subscribers
            return ResponseEntity.ok().body(APIResponse.<SendMessageResponse>builder()
                    .success(true)
                    .message("Message sent successfully")
                    .data(chatMessageService.sendMessage(request))
                    .build());

        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Error while sending message: " + e.getMessage());
        }
    }
}
