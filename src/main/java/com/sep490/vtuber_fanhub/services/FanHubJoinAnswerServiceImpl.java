package com.sep490.vtuber_fanhub.services;

import com.sep490.vtuber_fanhub.dto.requests.FanHubJoinAnswerRequest;
import com.sep490.vtuber_fanhub.dto.responses.FanHubJoinAnswerResponse;
import com.sep490.vtuber_fanhub.exceptions.NotFoundException;
import com.sep490.vtuber_fanhub.models.*;
import com.sep490.vtuber_fanhub.repositories.*;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class FanHubJoinAnswerServiceImpl implements FanHubJoinAnswerService {

    private final FanHubJoinAnswerRepository answerRepository;
    private final FanHubJoinQuestionRepository questionRepository;
    private final FanHubRepository fanHubRepository;
    private final FanHubMemberRepository memberRepository;
    private final AuthService authService;
    private final HttpServletRequest httpServletRequest;

    @Override
    @Transactional
    public String submitAnswers(Long hubId, List<FanHubJoinAnswerRequest> answers) {
        User currentUser = authService.getUserFromToken(httpServletRequest);
        
        FanHub hub = fanHubRepository.findByIdAndIsActive(hubId, true)
                .orElseThrow(() -> new NotFoundException("FanHub not found"));


        // Check if already a member
        Optional<FanHubMember> existingMember = memberRepository.findByHubIdAndUserId(hubId, currentUser.getId());
        if (existingMember.isPresent() && !"PENDING".equals(existingMember.get().getStatus())) {
            return "User is already a member or has a pending request for this FanHub";
        }

        // Get active questions
        List<FanHubJoinQuestion> questions = questionRepository.findActiveQuestionsByHubId(hubId);
        if (questions.isEmpty()) {
            throw new IllegalArgumentException("This FanHub does not have any questions to answer.");
        }

        if (answers == null || answers.size() < questions.size()) {
            throw new IllegalArgumentException("All questions must be answered.");
        }

        FanHubMember member;
        if (existingMember.isPresent()) {
            member = existingMember.get();
        } else {
            member = new FanHubMember();
            member.setHub(hub);
            member.setUser(currentUser);
            member.setJoinedAt(Instant.now());
            member.setFanHubScore(0);
        }

        // Set status based on hub requirements
        if (hub.getRequiresApproval() != null && hub.getRequiresApproval()) {
            member.setStatus("PENDING");
        } else {
            member.setStatus("JOINED");
            member.setRoleInHub("MEMBER");
        }
        
        memberRepository.save(member);

        // Save answers
        for (FanHubJoinAnswerRequest answerReq : answers) {
            FanHubJoinQuestion question = questionRepository.findById(answerReq.getQuestionId())
                    .orElseThrow(() -> new NotFoundException("Question not found with id: " + answerReq.getQuestionId()));
            
            if (!question.getHub().getId().equals(hubId)) {
                throw new IllegalArgumentException("Question does not belong to this FanHub");
            }

            FanHubJoinAnswer answer = new FanHubJoinAnswer();
            answer.setMember(member);
            answer.setQuestion(question);
            answer.setContent(answerReq.getContent());
            answerRepository.save(answer);
        }

        return hub.getRequiresApproval() != null && hub.getRequiresApproval()
                ? "Answers submitted. Join request awaiting approval."
                : "Joined FanHub successfully";
    }

    @Override
    @Transactional(readOnly = true)
    public List<FanHubJoinAnswerResponse> getAnswersByMemberId(Long memberId) {
        User currentUser = authService.getUserFromToken(httpServletRequest);
        FanHubMember targetMember = memberRepository.findById(memberId)
                .orElseThrow(() -> new NotFoundException("Member not found"));

        FanHub hub = targetMember.getHub();
        
        // Authorization: Owner, Moderator, or the member themselves
        boolean isOwner = hub.getOwnerUser().getId().equals(currentUser.getId());
        boolean isModerator = memberRepository.findByHubIdAndUserId(hub.getId(), currentUser.getId())
                .map(m -> "MODERATOR".equals(m.getRoleInHub()))
                .orElse(false);
        boolean isSelf = targetMember.getUser().getId().equals(currentUser.getId());

        if (!isOwner && !isModerator && !isSelf) {
            throw new AccessDeniedException("Access denied. You don't have permission to view these answers.");
        }

        return answerRepository.findByMemberId(memberId).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    private FanHubJoinAnswerResponse mapToResponse(FanHubJoinAnswer answer) {
        FanHubJoinAnswerResponse response = new FanHubJoinAnswerResponse();
        response.setId(answer.getId());
        response.setQuestionId(answer.getQuestion().getId());
        response.setQuestionContent(answer.getQuestion().getContent());
        response.setContent(answer.getContent());
        return response;
    }
}
