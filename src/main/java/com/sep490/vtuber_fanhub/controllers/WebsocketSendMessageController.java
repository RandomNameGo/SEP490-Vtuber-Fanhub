package com.sep490.vtuber_fanhub.controllers;

import com.sep490.vtuber_fanhub.dto.requests.SendMessageRequest;
import com.sep490.vtuber_fanhub.dto.responses.WebSocketChatMessageResponse;
import com.sep490.vtuber_fanhub.models.ChatMessage;
import com.sep490.vtuber_fanhub.models.ChatSession;
import com.sep490.vtuber_fanhub.models.User;
import com.sep490.vtuber_fanhub.repositories.ChatMessageRepository;
import com.sep490.vtuber_fanhub.repositories.ChatSessionRepository;
import com.sep490.vtuber_fanhub.repositories.UserRepository;
import com.sep490.vtuber_fanhub.services.JWTService;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Controller;

import java.security.Principal;
import java.time.Instant;
import java.util.Optional;

@Controller
@RequiredArgsConstructor
public class WebsocketSendMessageController {
    private final ChatSessionRepository chatSessionRepository;
    private final ChatMessageRepository chatMessageRepository;
    private final UserRepository userRepository;
    private final JWTService jwtService;

    /*
     * Frontend sends message to: /app/chat/sendMessage
     * Message is then broadcast to: /topic/chat/{sessionId}
     */
    @MessageMapping("/chat/sendMessage")
    @SendTo("/topic/chat")
    public WebSocketChatMessageResponse sendMessage(
            @Payload SendMessageRequest request,
            Principal principal
    ) {
        try {
            // Extract username from principal (WebSocket authentication)
            String username;
            if (principal instanceof UsernamePasswordAuthenticationToken) {
                username = principal.getName();
            } else if (principal != null) {
                username = principal.getName();
            } else {
                throw new RuntimeException("Authentication required");
            }

            Optional<User> userOpt = userRepository.findByUsernameAndIsActive(username);
            if (userOpt.isEmpty()) {
                throw new RuntimeException("User not found");
            }

            User user = userOpt.get();

            // Get or create chat session for the user
            ChatSession chatSession = chatSessionRepository.findByUser_Id(user.getId())
                    .orElseGet(() -> {
                        ChatSession newSession = new ChatSession();
                        newSession.setUser(user);
                        return chatSessionRepository.save(newSession);
                    });

            // Create and save the chat message
            ChatMessage chatMessage = new ChatMessage();
            chatMessage.setSenderRole("USER");
            chatMessage.setContent(request.getContent());
            chatMessage.setCreatedAt(Instant.now());
            chatMessage.setSession(chatSession);
            chatMessage = chatMessageRepository.save(chatMessage);

            // Return the message to be broadcast to all subscribers
            return WebSocketChatMessageResponse.builder()
                    .id(chatMessage.getId())
                    .senderRole(chatMessage.getSenderRole())
                    .content(chatMessage.getContent())
                    .createdAt(chatMessage.getCreatedAt())
                    .sessionId(chatSession.getId())
                    .build();

        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Error while sending message: " + e.getMessage());
        }
    }
}
