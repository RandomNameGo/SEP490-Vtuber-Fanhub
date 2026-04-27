package com.sep490.vtuber_fanhub.services;

import com.sep490.vtuber_fanhub.dto.requests.FanHubJoinQuestionRequest;
import com.sep490.vtuber_fanhub.dto.responses.FanHubJoinQuestionResponse;
import com.sep490.vtuber_fanhub.exceptions.NotFoundException;
import com.sep490.vtuber_fanhub.models.FanHub;
import com.sep490.vtuber_fanhub.models.FanHubJoinQuestion;
import com.sep490.vtuber_fanhub.models.User;
import com.sep490.vtuber_fanhub.repositories.FanHubJoinQuestionRepository;
import com.sep490.vtuber_fanhub.repositories.FanHubRepository;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class FanHubJoinQuestionServiceImpl implements FanHubJoinQuestionService {

    private final FanHubJoinQuestionRepository questionRepository;
    private final FanHubRepository fanHubRepository;
    private final AuthService authService;
    private final HttpServletRequest httpServletRequest;

    @Override
    @Transactional(readOnly = true)
    public List<FanHubJoinQuestionResponse> getQuestionsByHubId(Long hubId) {
        return questionRepository.findActiveQuestionsByHubId(hubId).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public String createQuestion(Long hubId, FanHubJoinQuestionRequest request) {
        User currentUser = authService.getUserFromToken(httpServletRequest);
        FanHub hub = fanHubRepository.findById(hubId)
                .orElseThrow(() -> new NotFoundException("FanHub not found"));

        validateOwnership(hub, currentUser);

        FanHubJoinQuestion question = new FanHubJoinQuestion();
        question.setHub(hub);
        question.setContent(request.getContent());
        question.setOrderNumber(request.getOrderNumber() != null ? request.getOrderNumber() : 0);
        question.setCreatedAt(Instant.now());
        question.setIsDeleted(false);
        
        questionRepository.save(question);
        return "Question created successfully";
    }

    @Override
    @Transactional
    public String updateQuestion(Long questionId, FanHubJoinQuestionRequest request) {
        User currentUser = authService.getUserFromToken(httpServletRequest);
        FanHubJoinQuestion question = questionRepository.findById(questionId)
                .orElseThrow(() -> new NotFoundException("Question not found or is deleted"));

        validateOwnership(question.getHub(), currentUser);

        if (request.getContent() != null) {
            question.setContent(request.getContent());
        }
        if (request.getOrderNumber() != null) {
            question.setOrderNumber(request.getOrderNumber());
        }
        
        questionRepository.save(question);
        return "Question updated successfully";
    }

    @Override
    @Transactional
    public String deleteQuestion(Long questionId) {
        User currentUser = authService.getUserFromToken(httpServletRequest);
        FanHubJoinQuestion question = questionRepository.findById(questionId)
                .orElseThrow(() -> new NotFoundException("Question not found or is deleted"));

        validateOwnership(question.getHub(), currentUser);

        question.setIsDeleted(true);
        questionRepository.save(question);
        return "Question deleted successfully";
    }

    private void validateOwnership(FanHub hub, User user) {
        if (!hub.getOwnerUser().getId().equals(user.getId())) {
            throw new AccessDeniedException("Access denied. Only the FanHub owner can manage questions.");
        }
    }

    private FanHubJoinQuestionResponse mapToResponse(FanHubJoinQuestion question) {
        FanHubJoinQuestionResponse response = new FanHubJoinQuestionResponse();
        response.setId(question.getId());
        response.setHubId(question.getHub().getId());
        response.setContent(question.getContent());
        response.setOrderNumber(question.getOrderNumber());
        response.setCreatedAt(question.getCreatedAt());
        return response;
    }
}
