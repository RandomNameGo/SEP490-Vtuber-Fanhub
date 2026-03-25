package com.sep490.vtuber_fanhub.services;

import com.sep490.vtuber_fanhub.dto.requests.SendMessageRequest;
import com.sep490.vtuber_fanhub.dto.responses.SendMessageResponse;
import com.sep490.vtuber_fanhub.exceptions.CustomAuthenticationException;
import com.sep490.vtuber_fanhub.models.ChatMessage;
import com.sep490.vtuber_fanhub.models.ChatSession;
import com.sep490.vtuber_fanhub.models.User;
import com.sep490.vtuber_fanhub.repositories.ChatMessageRepository;
import com.sep490.vtuber_fanhub.repositories.ChatSessionRepository;
import com.sep490.vtuber_fanhub.repositories.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ChatMessageServiceImpl implements ChatMessageService {

    private final JWTService jwtService;
    private final HttpServletRequest httpServletRequest;
    private final UserRepository userRepository;
    private final ChatSessionRepository chatSessionRepository;
    private final ChatMessageRepository chatMessageRepository;
    private final AiResponseService aiResponseService;

    @Override
    @Transactional
    public SendMessageResponse sendMessage(SendMessageRequest request) {
        try{
            String token = jwtService.getCurrentToken(httpServletRequest);
            String tokenUsername = jwtService.getUsernameFromToken(token);

            Optional<User> tokenUser = userRepository.findByUsernameAndIsActive(tokenUsername);
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

            return SendMessageResponse.builder()
                    .id(chatMessage.getId())
                    .result("Sent message success")
                    .build();
        }
        catch(Exception e){
            e.printStackTrace();
            throw new RuntimeException("Error while sending message");
        }
    }


}
