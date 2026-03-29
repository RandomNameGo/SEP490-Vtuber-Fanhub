package com.sep490.vtuber_fanhub.services;

import com.sep490.vtuber_fanhub.dto.responses.MessageResponse;
import com.sep490.vtuber_fanhub.exceptions.NotFoundException;
import com.sep490.vtuber_fanhub.models.ChatMessage;
import com.sep490.vtuber_fanhub.models.ChatSession;
import com.sep490.vtuber_fanhub.models.Enum.ChatPersonalityType;
import com.sep490.vtuber_fanhub.models.User;
import com.sep490.vtuber_fanhub.repositories.ChatMessageRepository;
import com.sep490.vtuber_fanhub.repositories.ChatSessionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AiResponseServiceImpl implements AiResponseService{
    private final ChatMessageRepository chatMessageRepository;
    private final ChatSessionRepository chatSessionRepository;
    private final GeminiAIService geminiAIService;
    private final SimpMessageSendingOperations messagingTemplate;
    private final ChatPersonalityType AI_CHATBOT_RESPONSE_PERSONALITY_TYPE = ChatPersonalityType.MatikanetannHauser;


    @Override
    @Transactional
    @Async("aiResponseExecutor")
    public void generateAndSendReply(User sender, String userMessageContent) {
        ChatSession chatSession = chatSessionRepository.findByUser_Id(sender.getId())
                .orElseThrow(()-> new NotFoundException("Chat session not found"));

        String aiResponse = smartChat(userMessageContent, sender.getId(), chatSession.getId());

        ChatMessage chatMessage = new ChatMessage();
        chatMessage.setSenderRole("AI");
        chatMessage.setCreatedAt(Instant.now());
        chatMessage.setContent(aiResponse);
        chatMessage.setSession(chatSession);
        chatMessage = chatMessageRepository.save(chatMessage);

        // Send AI response via WebSocket to /user/queue/reply
        // Use /user prefix to target the specific user's queue
        MessageResponse response = MessageResponse.builder()
                        .id(chatMessage.getId())
                        .createdAt(chatMessage.getCreatedAt())
                        .content(chatMessage.getContent())
                        .senderRole("AI")
                        .build();
        messagingTemplate.convertAndSendToUser(sender.getUsername(), "/queue/reply", response);
        
        // Also try sending directly to the queue without user prefix
        messagingTemplate.convertAndSend("/queue/reply", response);
    }

    @Override
    public String smartChat(String userPrompt, Long userId, Long sessionId) {
        List<ChatMessage> lastMessages = chatMessageRepository.findTop20BySession_Id(sessionId);

        return generateResponse(userPrompt, convertToPromptContext(lastMessages), AI_CHATBOT_RESPONSE_PERSONALITY_TYPE);
    }

    private String generateResponse(String userPrompt, String lastMessages, ChatPersonalityType personalityType) {
        String fullPrompt = String.format(""" 
            USER PROMPT: %s
            
            USER LAST_MESSAGES: %s
            
            """, userPrompt, lastMessages);

        return geminiAIService.sendPrompt(fullPrompt, personalityType);
    }

    public String convertToPromptContext(List<ChatMessage> messages) {
        if (messages == null || messages.isEmpty()) {
            return "";
        }

        return messages.stream()
                .map(msg -> {
                    String role = msg.getSenderRole().toUpperCase();
                    String text = msg.getContent();

                    // Format: ROLE: Content
                    // We use "Model" instead of "AI" to match Gemini's internal role names
                    String formattedRole = role.equals("USER") ? "User" : "Model";

                    StringBuilder sb = new StringBuilder();
                    sb.append(formattedRole).append(": ").append(text);

                    // Optional: If you want the AI to see its own previous "thoughts"
                    // to maintain consistency in reasoning.
                    if (msg.getThought() != null && !msg.getThought().isBlank()) {
                        sb.append("\n(Thought: ").append(msg.getThought()).append(")");
                    }

                    return sb.toString();
                })
                .collect(Collectors.joining("\n\n"));
    }


}
