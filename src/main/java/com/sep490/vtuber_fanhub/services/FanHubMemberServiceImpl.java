package com.sep490.vtuber_fanhub.services;

import com.sep490.vtuber_fanhub.dto.responses.FanHubMemberResponse;
import com.sep490.vtuber_fanhub.exceptions.CustomAuthenticationException;
import com.sep490.vtuber_fanhub.exceptions.NotFoundException;
import com.sep490.vtuber_fanhub.models.FanHub;
import com.sep490.vtuber_fanhub.models.FanHubMember;
import com.sep490.vtuber_fanhub.models.User;
import com.sep490.vtuber_fanhub.repositories.FanHubMemberRepository;
import com.sep490.vtuber_fanhub.repositories.FanHubRepository;
import com.sep490.vtuber_fanhub.repositories.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class FanHubMemberServiceImpl implements FanHubMemberService {

    private final FanHubMemberRepository fanHubMemberRepository;

    private final FanHubRepository fanHubRepository;

    private final UserRepository userRepository;

    private final JWTService jwtService;

    private final HttpServletRequest httpServletRequest;

    @Override
    @Transactional
    public String joinFanHubMember(long fanHubId) {
        String token = jwtService.getCurrentToken(httpServletRequest);
        String tokenUsername = jwtService.getUsernameFromToken(token);

        Optional<User> tokenUser = userRepository.findByUsernameAndIsActive(tokenUsername);
        if (tokenUser.isEmpty()) {
            throw new CustomAuthenticationException("Authentication failed");
        }

        Optional<FanHub> fanHub = fanHubRepository.findById(fanHubId);
        if (fanHub.isEmpty()) {
            throw new NotFoundException("FanHub not found");
        }

        // Check if user is already a member
        Optional<FanHubMember> existingMember = fanHubMemberRepository.findByHubIdAndUserId(
                fanHubId, tokenUser.get().getId());
        if (existingMember.isPresent()) {
            throw new CustomAuthenticationException("User is already a member of this FanHub");
        }

        FanHubMember member = new FanHubMember();
        member.setHub(fanHub.get());
        member.setUser(tokenUser.get());
        member.setJoinedAt(Instant.now());
        member.setFanHubScore(0);

        if (fanHub.get().getRequiresApproval() != null && fanHub.get().getRequiresApproval()) {
            member.setStatus("PENDING");
        } else {
            member.setStatus("JOINED");
            member.setRoleInHub("MEMBER");
        }

        fanHubMemberRepository.save(member);

        return fanHub.get().getRequiresApproval() != null && fanHub.get().getRequiresApproval()
                ? "Join request submitted. Awaiting approval."
                : "Joined FanHub successfully";
    }

    @Override
    public List<FanHubMemberResponse> getFanHubMembers(long fanHubId, int pageNo, int pageSize, String sortBy) {
        String token = jwtService.getCurrentToken(httpServletRequest);
        String tokenUsername = jwtService.getUsernameFromToken(token);

        Optional<User> tokenUser = userRepository.findByUsernameAndIsActive(tokenUsername);
        if (tokenUser.isEmpty()) {
            throw new CustomAuthenticationException("Authentication failed");
        }

        Optional<FanHub> fanHub = fanHubRepository.findById(fanHubId);
        if (fanHub.isEmpty()) {
            throw new NotFoundException("FanHub not found");
        }

        // Check if user is VTUBER and owns this FanHub
        boolean isOwner = "VTUBER".equals(tokenUser.get().getRole()) &&
                fanHub.get().getOwnerUser().getId().equals(tokenUser.get().getId());

        // Check if user is a member with MODERATOR role
        boolean isModerator = fanHubMemberRepository.findByHubIdAndUserId(fanHubId, tokenUser.get().getId())
                .map(member -> "MODERATOR".equals(member.getRoleInHub()))
                .orElse(false);

        if (!isOwner && !isModerator) {
            throw new CustomAuthenticationException("Access denied");
        }

        Pageable paging = PageRequest.of(pageNo, pageSize, Sort.by(sortBy));

        Page<FanHubMember> pagedMembers = fanHubMemberRepository.findByHubId(fanHubId, paging);

        if (pagedMembers.hasContent()) {
            return pagedMembers.getContent().stream()
                    .map(this::mapToResponse)
                    .collect(Collectors.toList());
        }

        return List.of();
    }

    @Override
    public List<FanHubMemberResponse> getPendingFanHubMembers(long fanHubId, int pageNo, int pageSize, String sortBy) {
        String token = jwtService.getCurrentToken(httpServletRequest);
        String tokenUsername = jwtService.getUsernameFromToken(token);

        Optional<User> tokenUser = userRepository.findByUsernameAndIsActive(tokenUsername);
        if (tokenUser.isEmpty()) {
            throw new CustomAuthenticationException("Authentication failed");
        }

        Optional<FanHub> fanHub = fanHubRepository.findById(fanHubId);
        if (fanHub.isEmpty()) {
            throw new NotFoundException("FanHub not found");
        }

        // Check if user is VTUBER and owns this FanHub
        boolean isOwner = "VTUBER".equals(tokenUser.get().getRole()) &&
                fanHub.get().getOwnerUser().getId().equals(tokenUser.get().getId());

        // Check if user is a member with MODERATOR role
        boolean isModerator = fanHubMemberRepository.findByHubIdAndUserId(fanHubId, tokenUser.get().getId())
                .map(member -> "MODERATOR".equals(member.getRoleInHub()))
                .orElse(false);

        if (!isOwner && !isModerator) {
            throw new CustomAuthenticationException("Access denied");
        }

        Pageable paging = PageRequest.of(pageNo, pageSize, Sort.by(sortBy));

        Page<FanHubMember> pagedMembers = fanHubMemberRepository.findByHubIdAndStatus(fanHubId, "PENDING", paging);

        if (pagedMembers.hasContent()) {
            return pagedMembers.getContent().stream()
                    .map(this::mapToResponse)
                    .collect(Collectors.toList());
        }

        return List.of();
    }

    private FanHubMemberResponse mapToResponse(FanHubMember entity) {
        FanHubMemberResponse response = new FanHubMemberResponse();

        response.setId(entity.getId());

        if (entity.getHub() != null) {
            response.setHubId(entity.getHub().getId());
            response.setHubName(entity.getHub().getHubName());
        }

        if (entity.getUser() != null) {
            response.setUserId(entity.getUser().getId());
            response.setUsername(entity.getUser().getUsername());
            response.setDisplayName(entity.getUser().getDisplayName());
        }

        response.setRoleInHub(entity.getRoleInHub());
        response.setStatus(entity.getStatus());
        response.setFanHubScore(entity.getFanHubScore());
        response.setJoinedAt(entity.getJoinedAt());
        response.setTitle(entity.getTitle());

        return response;
    }
}
