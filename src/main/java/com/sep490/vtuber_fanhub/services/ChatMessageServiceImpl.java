package com.sep490.vtuber_fanhub.services;

import com.sep490.vtuber_fanhub.dto.requests.SendMessageRequest;
import com.sep490.vtuber_fanhub.dto.responses.MessageResponse;
import com.sep490.vtuber_fanhub.exceptions.CustomAuthenticationException;
import com.sep490.vtuber_fanhub.models.ChatMessage;
import com.sep490.vtuber_fanhub.models.ChatSession;
import com.sep490.vtuber_fanhub.models.User;
import com.sep490.vtuber_fanhub.repositories.ChatMessageRepository;
import com.sep490.vtuber_fanhub.repositories.ChatSessionRepository;
import com.sep490.vtuber_fanhub.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ChatMessageServiceImpl implements ChatMessageService {

    private final UserRepository userRepository;
    private final ChatSessionRepository chatSessionRepository;
    private final ChatMessageRepository chatMessageRepository;
    private final AiResponseService aiResponseService;

    @Override
    @Transactional
    public MessageResponse sendMessage(SendMessageRequest request, String username) {
        try{
            Optional<User> tokenUser = userRepository.findByUsernameAndIsActive(username);
            if (tokenUser.isEmpty()) {
                throw new CustomAuthenticationException("Authentication failed");
            }

            String message = request.getContent();

            User user = tokenUser.get();

            ChatSession chatSession;

            Optional<ChatSession> session = chatSessionRepository.findByUser_Id(user.getId());
            if(session.isEmpty()) {
                ChatSession newSession = new ChatSession();
                newSession.setUser(user);
                newSession = chatSessionRepository.save(newSession);
                chatSession = newSession;
            }
            else chatSession = session.get();

            ChatMessage chatMessage = new ChatMessage();
            chatMessage.setSenderRole("USER");
            chatMessage.setCreatedAt(Instant.now());
            chatMessage.setContent(message);
            chatMessage.setSession(chatSession);
            chatMessage = chatMessageRepository.save(chatMessage);

            aiResponseService.generateAndSendReply(user, message);

            return MessageResponse.builder()
                    .id(chatMessage.getId())
                    .content(chatMessage.getContent())
                    .createdAt(chatMessage.getCreatedAt())
                    .senderRole("USER")
                    .build();
        }
        catch(Exception e){
            e.printStackTrace();
            throw new RuntimeException("Error while sending message");
        }
    }

    @Override
    public List<MessageResponse> getAllMessages(String username) {
        try{
            Optional<User> tokenUser = userRepository.findByUsernameAndIsActive(username);
            if (tokenUser.isEmpty()) {
                throw new CustomAuthenticationException("Authentication failed");
            }

            Optional<ChatSession> chatSession = chatSessionRepository.findByUser_Id(tokenUser.get().getId());
            if (chatSession.isEmpty()) {
                return null;
            }

            List<ChatMessage> chatMessages = chatMessageRepository.getAllMessagesBySession_Id(chatSession.get().getId());
            List<MessageResponse> chatMessageResponses = new ArrayList<>();
            for (ChatMessage chatMessage : chatMessages) {
                MessageResponse response = MessageResponse.builder()
                        .id(chatMessage.getId())
                        .createdAt(chatMessage.getCreatedAt())
                        .content(chatMessage.getContent())
                        .senderRole(chatMessage.getSenderRole())
                        .thought(chatMessage.getThought())
                        .build();
                chatMessageResponses.add(response);
            }
            return chatMessageResponses;

        }catch(Exception e){
            e.printStackTrace();
            System.out.println("Error while getting all messages");
            return null;
        }
    }


}
