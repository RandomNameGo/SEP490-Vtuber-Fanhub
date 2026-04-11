package com.sep490.vtuber_fanhub.services;

import com.sep490.vtuber_fanhub.dto.requests.SendMessageRequest;
import com.sep490.vtuber_fanhub.dto.responses.MessageResponse;
import com.sep490.vtuber_fanhub.dto.responses.PaginatedResponse;
import com.sep490.vtuber_fanhub.exceptions.CustomAuthenticationException;
import com.sep490.vtuber_fanhub.models.ChatMessage;
import com.sep490.vtuber_fanhub.models.ChatSession;
import com.sep490.vtuber_fanhub.models.User;
import com.sep490.vtuber_fanhub.repositories.ChatMessageRepository;
import com.sep490.vtuber_fanhub.repositories.ChatSessionRepository;
import com.sep490.vtuber_fanhub.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

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
            chatMessageRepository.save(chatMessage);

            return aiResponseService.generateAndSendReply(user, message);
        }
        catch(Exception e){
            throw new RuntimeException("Error while sending message");
        }
    }

    @Override
    public PaginatedResponse<MessageResponse> getMessagesPaginated(String username, int page, int size) {
        try {
            Optional<User> tokenUser = userRepository.findByUsernameAndIsActive(username);
            if (tokenUser.isEmpty()) {
                throw new CustomAuthenticationException("Authentication failed");
            }

            Optional<ChatSession> chatSession = chatSessionRepository.findByUser_Id(tokenUser.get().getId());
            if (chatSession.isEmpty()) {
                return PaginatedResponse.<MessageResponse>builder()
                        .data(new ArrayList<>())
                        .currentPage(page)
                        .pageSize(size)
                        .totalElements(0)
                        .totalPages(0)
                        .hasNext(false)
                        .hasPrevious(false)
                        .build();
            }

            // Sort by createdAt descending (newest first)
            PageRequest pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
            Page<ChatMessage> messagesPage = chatMessageRepository.findAllBySession_Id(chatSession.get().getId(), pageable);

            List<MessageResponse> messageResponses = messagesPage.getContent().stream()
                    .map(chatMessage -> MessageResponse.builder()
                            .id(chatMessage.getId())
                            .createdAt(chatMessage.getCreatedAt())
                            .content(chatMessage.getContent())
                            .senderRole(chatMessage.getSenderRole())
                            .thought(chatMessage.getThought())
                            .build())
                    .collect(Collectors.toList());

            return PaginatedResponse.<MessageResponse>builder()
                    .data(messageResponses)
                    .currentPage(page)
                    .pageSize(size)
                    .totalElements(messagesPage.getTotalElements())
                    .totalPages(messagesPage.getTotalPages())
                    .hasNext(messagesPage.hasNext())
                    .hasPrevious(messagesPage.hasPrevious())
                    .build();

        } catch (Exception e) {
            System.out.println("Error while getting paginated messages: " + e.getMessage());
            return null;
        }
    }

}
