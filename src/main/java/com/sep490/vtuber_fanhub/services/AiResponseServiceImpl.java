package com.sep490.vtuber_fanhub.services;

import com.sep490.vtuber_fanhub.dto.responses.AIMessageResponse;
import com.sep490.vtuber_fanhub.dto.responses.MessageResponse;
import com.sep490.vtuber_fanhub.models.ChatMessage;
import com.sep490.vtuber_fanhub.models.ChatMessageMetadata;
import com.sep490.vtuber_fanhub.models.ChatSession;
import com.sep490.vtuber_fanhub.models.Enum.ChatPersonalityType;
import com.sep490.vtuber_fanhub.models.Enum.MetadataType;
import com.sep490.vtuber_fanhub.models.User;
import com.sep490.vtuber_fanhub.repositories.ChatMessageMetadataRepository;
import com.sep490.vtuber_fanhub.repositories.ChatMessageRepository;
import com.sep490.vtuber_fanhub.repositories.ChatSessionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AiResponseServiceImpl implements AiResponseService {
    private final ChatMessageRepository chatMessageRepository;
    private final ChatSessionRepository chatSessionRepository;
    private final GeminiAIService geminiAIService;
    private final ChatPersonalityType AI_CHATBOT_RESPONSE_PERSONALITY_TYPE = ChatPersonalityType.MatikanetannHauser;
    private final ChatMessageMetadataRepository chatMessageMetadataRepository;

    @Override
    @Transactional
    public MessageResponse generateAndSendReply(User sender, String userMessageContent) {
        try{
            ChatSession chatSession;
            Optional<ChatSession> existingSession = chatSessionRepository.findByUser_Id(sender.getId());
            if(existingSession.isEmpty()){
                chatSession = new ChatSession();
                chatSession.setUser(sender);
                chatSession = chatSessionRepository.save(chatSession);
            }else chatSession = existingSession.get();

            AIMessageResponse aiResponse = smartChat(userMessageContent, sender.getId(), chatSession.getId());

            ChatMessage chatMessage = new ChatMessage();
            chatMessage.setSenderRole("AI");
            chatMessage.setCreatedAt(Instant.now());
            chatMessage.setContent(aiResponse.getMessage());
            chatMessage.setThought(aiResponse.getThought());
            chatMessage.setSession(chatSession);
            chatMessage = chatMessageRepository.save(chatMessage);

            if(aiResponse.isHasMetadata()){
                ChatMessageMetadata chatMessageMetadata = new ChatMessageMetadata();
                chatMessageMetadata.setMessage(chatMessage);
                chatMessageMetadata.setMetadataType(MetadataType.POST);
                chatMessageMetadata.setTargetId(aiResponse.getMetadataTargetId());
                chatMessageMetadataRepository.save(chatMessageMetadata);

                chatMessage.setHasMetadata(true);
                chatMessage = chatMessageRepository.save(chatMessage);
            }

            MessageResponse.MessageResponseBuilder responseBuilder = MessageResponse.builder();
            responseBuilder
                    .id(chatMessage.getId())
                    .createdAt(chatMessage.getCreatedAt())
                    .content(chatMessage.getContent())
                    .senderRole("AI");

            if(aiResponse.getThought()!=null){
                responseBuilder.thought(aiResponse.getThought());
            }

            if(aiResponse.isHasMetadata()){
                responseBuilder
                        .hasMetadata(true)
                        .metadataTargetId(aiResponse.getMetadataTargetId())
                        .metadataType(aiResponse.getMetadataType());
            }else responseBuilder.hasMetadata(false);

            return responseBuilder.build();
            
        }catch(Exception e){
            e.printStackTrace();
            throw new RuntimeException("Error while generating and sending reply to user");
        }
    }

    public AIMessageResponse smartChat(String userPrompt, Long userId, Long sessionId) {
        List<ChatMessage> lastMessages = chatMessageRepository.findTop20BySession_Id(sessionId);

        return generateResponse(userPrompt, convertToPromptContext(lastMessages), AI_CHATBOT_RESPONSE_PERSONALITY_TYPE, userId);
    }

    // user id is required for function calling
    private AIMessageResponse generateResponse(String userPrompt, String lastMessages, ChatPersonalityType personalityType, Long userId) {
        String fullPrompt = String.format(""" 
            USER PROMPT: %s
            
            USER LAST_MESSAGES: %s
            
            """, userPrompt, lastMessages);

        return geminiAIService.sendPromptFunctionCalling(fullPrompt, personalityType, userId);
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
