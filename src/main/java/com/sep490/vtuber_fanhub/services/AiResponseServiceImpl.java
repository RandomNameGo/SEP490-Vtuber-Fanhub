package com.sep490.vtuber_fanhub.services;

import com.sep490.vtuber_fanhub.dto.responses.AIMessageResponse;
import com.sep490.vtuber_fanhub.dto.responses.MessageResponse;
import com.sep490.vtuber_fanhub.dto.responses.MetadataResponse;
import com.sep490.vtuber_fanhub.models.*;
import com.sep490.vtuber_fanhub.models.Enum.ChatPersonalityType;
import com.sep490.vtuber_fanhub.models.Enum.MetadataType;
import com.sep490.vtuber_fanhub.repositories.*;
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
    private final PostRepository postRepository;
    private final PostMediaRepository postMediaRepository;
    private final FanHubRepository fanHubRepository;
    private final GeminiAIService geminiAIService;
    private final ChatPersonalityType AI_CHATBOT_RESPONSE_PERSONALITY_TYPE = ChatPersonalityType.VGuide;
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
                chatMessageMetadata.setMetadataType(aiResponse.getMetadataType());
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
                switch (aiResponse.getMetadataType()) {
                    case POST -> postRepository.findById(aiResponse.getMetadataTargetId())
                            .ifPresent(post -> {
                                // if the post is image type, return additional preview image url
                                String mediaPreviewUrl = "";
                                if(post.getPostType().equals("IMAGE")){
                                    List<PostMedia> medias = postMediaRepository.findByPostId(post.getId());
                                    if(!medias.isEmpty()){
                                        mediaPreviewUrl = medias.get(0).getMediaUrl();
                                    }

                                }
                                responseBuilder.metadataResponse(MetadataResponse.builder()
                                        .metadataType(aiResponse.getMetadataType())
                                        .postId(post.getId())
                                        .postTitle(post.getTitle())
                                        .postContent(post.getContent())
                                        .imagePreviewUrl(mediaPreviewUrl)
                                        .build());
                            });
                    case HUB -> fanHubRepository.findById(aiResponse.getMetadataTargetId())
                            .ifPresent(fanHub -> {
                                responseBuilder.metadataResponse(MetadataResponse.builder()
                                        .metadataType(aiResponse.getMetadataType())
                                        .fanHubId(fanHub.getId())
                                        .subdomain(fanHub.getSubdomain())
                                        .hubName(fanHub.getHubName())
                                        .avatarUrl(fanHub.getAvatarUrl())
                                        .bannerUrl(fanHub.getBannerUrl())
                                        .build());
                            });
                }

            }

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
    ROLE: You are a VTuber Community Guide for FanHub.
    
    CORE RULE: Your knowledge and conversations are STRICTLY limited to the world of VTubers (Virtual YouTubers), 
    streaming culture, anime-style avatars, and fan communities.
    
    DOMAIN GUARDRAIL:
    - If a user asks about topics outside the VTuber world (e.g., general history, non-VTuber literature, 
      politics, or general math), gracefully pivot the conversation back to VTubers.
    - Example for off-topic questions: "That's interesting, but as a VTuber superfan, I'd rather talk about [Related VTuber Topic]!"
    - Never break character. You don't know things outside the virtual world.
    
    INSTRUCTIONS:
    - Language: Always respond in the same language as the USER PROMPT.
    - Context: Use LAST_MESSAGES for continuity, but never meta-comment on them (don't say "As I mentioned before").
    - Style: Conversational, enthusiastic, and helpful. Use common VTuber terminology (debut, lore, graduation, clipping, oshi).
    - Reply short and precise.
    
    RESPONSE PARSING RULES:
    - Bold: **text**
    - Italic: *text*
    - Underline: __text__
    - Strikethrough: ~~text~~
    
    USER PROMPT: %s
    LAST_MESSAGES: %s
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
