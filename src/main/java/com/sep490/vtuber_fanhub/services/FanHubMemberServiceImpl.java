package com.sep490.vtuber_fanhub.services;

import com.sep490.vtuber_fanhub.dto.responses.FanHubMemberResponse;
import com.sep490.vtuber_fanhub.dto.responses.FanHubMembershipResponse;
import com.sep490.vtuber_fanhub.dto.responses.MemberDetailResponse;
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
import org.springframework.security.access.AccessDeniedException;
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

    private final HttpServletRequest httpServletRequest;

    private final AuthService authService;

    private final BanMemberService banMemberService;

    @Override
    @Transactional
    public String joinFanHubMember(long fanHubId) {
        User currentUser = authService.getUserFromToken(httpServletRequest);

        Optional<FanHub> fanHub = fanHubRepository.findById(fanHubId);
        if (fanHub.isEmpty()) {
            throw new NotFoundException("FanHub not found");
        }

        // Check if user is banned from joining this hub
        banMemberService.checkBanStatus(fanHubId, currentUser.getId(), List.of("JOIN"));

        // Check if user is already a member
        Optional<FanHubMember> existingMember = fanHubMemberRepository.findByHubIdAndUserId(
                fanHubId, currentUser.getId());
        if (existingMember.isPresent()) {
            return "User is already a member of this FanHub";
        }

        FanHubMember member = new FanHubMember();
        member.setHub(fanHub.get());
        member.setUser(currentUser);
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
    public List<FanHubMemberResponse> getFanHubMembers(long fanHubId, int pageNo, int pageSize, String sortBy, String username) {
        User currentUser = authService.getUserFromToken(httpServletRequest);

        Optional<FanHub> fanHub = fanHubRepository.findById(fanHubId);
        if (fanHub.isEmpty()) {
            throw new NotFoundException("FanHub not found");
        }

        // Check if user is a member of this FanHub
        Optional<FanHubMember> currentMember = fanHubMemberRepository.findByHubIdAndUserId(fanHubId, currentUser.getId());
        boolean isUserMember = "USER".equals(currentUser.getRole()) && currentMember.isPresent();

        // Check if user is VTUBER and owns this FanHub
        boolean isOwner = "VTUBER".equals(currentUser.getRole()) &&
                fanHub.get().getOwnerUser().getId().equals(currentUser.getId());

        if (!isUserMember && !isOwner) {
            throw new AccessDeniedException("Access denied");
        }

        Pageable paging = PageRequest.of(pageNo, pageSize, Sort.by(sortBy));

        Page<FanHubMember> pagedMembers;
        if (username != null && !username.isEmpty()) {
            pagedMembers = fanHubMemberRepository.findByHubIdAndUsername(fanHubId, username, paging);
        } else {
            pagedMembers = fanHubMemberRepository.findByHubId(fanHubId, paging);
        }

        if (pagedMembers.hasContent()) {
            return pagedMembers.getContent().stream()
                    .map(this::mapToResponse)
                    .collect(Collectors.toList());
        }

        return List.of();
    }

    @Override
    public List<FanHubMemberResponse> getPendingFanHubMembers(long fanHubId, int pageNo, int pageSize, String sortBy) {
        User currentUser = authService.getUserFromToken(httpServletRequest);

        Optional<FanHub> fanHub = fanHubRepository.findById(fanHubId);
        if (fanHub.isEmpty()) {
            throw new NotFoundException("FanHub not found");
        }

        // Check if user is VTUBER and owns this FanHub
        boolean isOwner = "VTUBER".equals(currentUser.getRole()) &&
                fanHub.get().getOwnerUser().getId().equals(currentUser.getId());

        // Check if user is a member with MODERATOR role
        boolean isModerator = fanHubMemberRepository.findByHubIdAndUserId(fanHubId, currentUser.getId())
                .map(member -> "MODERATOR".equals(member.getRoleInHub()))
                .orElse(false);

        if (!isOwner && !isModerator) {
            throw new AccessDeniedException("Access denied");
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

    @Override
    @Transactional
    public String addModerator(long fanHubId, List<Long> fanHubMemberIds) {

        User currentUser = authService.getUserFromToken(httpServletRequest);

        Optional<FanHub> fanHub = fanHubRepository.findById(fanHubId);
        if (fanHub.isEmpty()) {
            throw new NotFoundException("FanHub not found");
        }

        boolean isOwner = "VTUBER".equals(currentUser.getRole()) &&
                fanHub.get().getOwnerUser().getId().equals(currentUser.getId());

        if (!isOwner) {
            throw new AccessDeniedException("Access denied");
        }

        for (Long fanHubMemberId : fanHubMemberIds) {
            Optional<FanHubMember> member = fanHubMemberRepository.findById(fanHubMemberId);
            if (member.isPresent()) {
                member.get().setRoleInHub("MODERATOR");
                fanHubMemberRepository.save(member.get());
            } else {
                throw new NotFoundException("Member not found");
            }
        }

        return "Set moderator successfully";
    }

    @Override
    @Transactional
    public String removeModerator(long fanHubId, List<Long> fanHubMemberIds) {

        User currentUser = authService.getUserFromToken(httpServletRequest);

        Optional<FanHub> fanHub = fanHubRepository.findById(fanHubId);
        if (fanHub.isEmpty()) {
            throw new NotFoundException("FanHub not found");
        }

        boolean isOwner = "VTUBER".equals(currentUser.getRole()) &&
                fanHub.get().getOwnerUser().getId().equals(currentUser.getId());

        if (!isOwner) {
            throw new AccessDeniedException("Access denied");
        }

        for (Long fanHubMemberId : fanHubMemberIds) {
            Optional<FanHubMember> member = fanHubMemberRepository.findById(fanHubMemberId);
            if (member.isPresent()) {
                if (!"MODERATOR".equals(member.get().getRoleInHub())) {
                    throw new IllegalArgumentException("Member is not a moderator");
                }
                member.get().setRoleInHub("MEMBER");
                fanHubMemberRepository.save(member.get());
            } else {
                throw new NotFoundException("Member not found");
            }
        }

        return "Remove moderator successfully";
    }

    @Override
    @Transactional
    public String reviewFanHubMember(long fanHubMemberId, String status) {
        User currentUser = authService.getUserFromToken(httpServletRequest);

        Optional<FanHubMember> member = fanHubMemberRepository.findById(fanHubMemberId);
        if (member.isEmpty()) {
            throw new NotFoundException("FanHub member not found");
        }

        // Validate status parameter
        String normalizedStatus = status.toUpperCase();
        if (!List.of("APPROVED", "REJECTED").contains(normalizedStatus)) {
            throw new IllegalArgumentException("Invalid status. Must be APPROVED or REJECTED");
        }

        Long fanHubId = member.get().getHub().getId();

        // Check if user is VTUBER and owns this FanHub
        boolean isOwner = "VTUBER".equals(currentUser.getRole()) &&
                fanHubRepository.findById(fanHubId)
                        .map(hub -> hub.getOwnerUser().getId().equals(currentUser.getId()))
                        .orElse(false);

        // Check if user is a member with MODERATOR role
        boolean isModerator = fanHubMemberRepository.findByHubIdAndUserId(fanHubId, currentUser.getId())
                .map(m -> "MODERATOR".equals(m.getRoleInHub()))
                .orElse(false);

        if (!isOwner && !isModerator) {
            throw new AccessDeniedException("Only VTUBER (owner) or MODERATOR can review membership requests");
        }

        // Check if member is in PENDING status
        if (!"PENDING".equals(member.get().getStatus())) {
            throw new IllegalArgumentException("Member request is not pending");
        }

        if ("APPROVED".equals(normalizedStatus)) {
            member.get().setStatus("JOINED");
            member.get().setRoleInHub("MEMBER");
        } else {
            member.get().setStatus("REJECTED");
        }
        fanHubMemberRepository.save(member.get());

        return "Membership request " + normalizedStatus.toLowerCase() + " successfully";
    }

    @Override
    @Transactional(readOnly = true)
    public MemberDetailResponse getMemberDetail(long fanHubMemberId) {
        User currentUser = authService.getUserFromToken(httpServletRequest);

        Optional<FanHubMember> member = fanHubMemberRepository.findById(fanHubMemberId);
        if (member.isEmpty()) {
            throw new NotFoundException("FanHub member not found");
        }

        Long fanHubId = member.get().getHub().getId();

        // Check if user is VTUBER and owns this FanHub
        boolean isOwner = "VTUBER".equals(currentUser.getRole()) &&
                fanHubRepository.findById(fanHubId)
                        .map(hub -> hub.getOwnerUser().getId().equals(currentUser.getId()))
                        .orElse(false);

        // Check if user is a member with MODERATOR role
        boolean isModerator = fanHubMemberRepository.findByHubIdAndUserId(fanHubId, currentUser.getId())
                .map(m -> "MODERATOR".equals(m.getRoleInHub()))
                .orElse(false);

        if (!isOwner && !isModerator) {
            throw new AccessDeniedException("Only VTUBER (owner) or MODERATOR can view member details");
        }

        return mapToMemberDetailResponse(member.get());
    }

    @Override
    @Transactional(readOnly = true)
    public FanHubMembershipResponse checkUserMembership(Long fanHubId) {
        User currentUser = authService.getUserFromToken(httpServletRequest);

        // Check if fan hub exists
        FanHub fanHub = fanHubRepository.findById(fanHubId)
                .orElseThrow(() -> new NotFoundException("FanHub not found"));

        FanHubMembershipResponse response = new FanHubMembershipResponse();

        // Check if user is the owner of the FanHub
        if (fanHub.getOwnerUser().getId().equals(currentUser.getId())) {
            response.setIsMember(true);
            response.setRoleInHub("VTUBER");
            return response;
        }

        // Check if user is a member with JOINED status
        Optional<FanHubMember> member = fanHubMemberRepository.findByHubIdAndUserId(fanHubId, currentUser.getId())
                .filter(m -> "JOINED".equals(m.getStatus()));

        response.setIsMember(member.isPresent());
        response.setRoleInHub(member.isPresent() ? member.get().getRoleInHub() : null);

        return response;
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

    private MemberDetailResponse mapToMemberDetailResponse(FanHubMember entity) {
        MemberDetailResponse response = new MemberDetailResponse();

        // FanHubMember fields
        response.setMemberId(entity.getId());

        if (entity.getHub() != null) {
            response.setHubId(entity.getHub().getId());
            response.setHubName(entity.getHub().getHubName());
        }

        response.setRoleInHub(entity.getRoleInHub());
        response.setStatus(entity.getStatus());
        response.setFanHubScore(entity.getFanHubScore());
        response.setJoinedAt(entity.getJoinedAt());
        response.setTitle(entity.getTitle());

        // User fields
        if (entity.getUser() != null) {
            User user = entity.getUser();
            response.setUserId(user.getId());
            response.setUsername(user.getUsername());
            response.setEmail(user.getEmail());
            response.setDisplayName(user.getDisplayName());
            response.setAvatarUrl(user.getAvatarUrl());
            response.setFrameUrl(user.getFrameUrl());
        }

        return response;
    }
}
