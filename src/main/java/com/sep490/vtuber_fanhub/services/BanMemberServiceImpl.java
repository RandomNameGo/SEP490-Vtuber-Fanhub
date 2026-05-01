package com.sep490.vtuber_fanhub.services;

import com.sep490.vtuber_fanhub.dto.requests.CreateBanMemberRequest;
import com.sep490.vtuber_fanhub.dto.responses.BanMemberResponse;
import com.sep490.vtuber_fanhub.dto.responses.MemberWithBansResponse;
import com.sep490.vtuber_fanhub.exceptions.NotFoundException;
import com.sep490.vtuber_fanhub.models.BanMember;
import com.sep490.vtuber_fanhub.models.FanHub;
import com.sep490.vtuber_fanhub.models.FanHubMember;
import com.sep490.vtuber_fanhub.models.User;
import com.sep490.vtuber_fanhub.repositories.BanMemberRepository;
import com.sep490.vtuber_fanhub.repositories.FanHubMemberRepository;
import com.sep490.vtuber_fanhub.repositories.FanHubRepository;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class BanMemberServiceImpl implements BanMemberService {

    private final BanMemberRepository banMemberRepository;

    private final FanHubMemberRepository fanHubMemberRepository;

    private final FanHubRepository fanHubRepository;

    private final AuthService authService;

    private final HttpServletRequest httpServletRequest;

    private final NotificationService notificationService;

    @Override
    @Transactional
    public String banFanHubMember(CreateBanMemberRequest request) {

        User currentUser = authService.getUserFromToken(httpServletRequest);

        FanHubMember fanHubMember = fanHubMemberRepository.findById(request.getFanHubMemberId())
                .orElseThrow(() -> new NotFoundException("Fan hub member not found"));

        Long fanHubId = fanHubMember.getHub().getId();

        // Check if user is VTUBER and owns this FanHub
        boolean isOwner = "VTUBER".equals(currentUser.getRole()) &&
                fanHubRepository.findById(fanHubId)
                        .map(hub -> hub.getOwnerUser().getId().equals(currentUser.getId()))
                        .orElse(false);

        // Check if user is a member with MODERATOR role
        boolean isModerator = fanHubMemberRepository.findByHubIdAndUserId(fanHubId, currentUser.getId())
                .map(member -> "MODERATOR".equals(member.getRoleInHub()))
                .orElse(false);

        if (!isOwner && !isModerator) {
            throw new AccessDeniedException("Access denied");
        }

        String banType = request.getBanType().toUpperCase();

        // Check for existing active ban of the same type
        Optional<BanMember> existingBanOpt = banMemberRepository
                .findByHubIdAndUserIdAndBanTypeAndIsActiveTrue(fanHubId, fanHubMember.getUser().getId(), banType);

        if (existingBanOpt.isPresent()) {
            BanMember existingBan = existingBanOpt.get();
            Instant existingUntil = existingBan.getBannedUntil();
            Instant newUntil = request.getBannedUntil();

            // Compare durations
            boolean isNewLonger = false;
            if (existingUntil == null) {
                // Existing is permanent, nothing can be longer
                isNewLonger = false;
            } else if (newUntil == null) {
                // New is permanent, existing is not
                isNewLonger = true;
            } else {
                // Both have expiry, compare dates
                isNewLonger = newUntil.isAfter(existingUntil);
            }

            if (!isNewLonger) {
                return "Member already has a longer or equal ban of this type";
            }

            // Deactivate the old ban
            existingBan.setIsActive(false);
            banMemberRepository.save(existingBan);
        }

        BanMember banMember = new BanMember();
        banMember.setHub(fanHubMember.getHub());
        banMember.setUser(fanHubMember.getUser());
        banMember.setBannedBy(currentUser);
        banMember.setReason(request.getReason());
        banMember.setBanType(banType);
        banMember.setBannedUntil(request.getBannedUntil());
        banMember.setIsActive(true);
        banMember.setCreatedAt(Instant.now());
        banMemberRepository.save(banMember);

        // Send notification to the banned user
        notificationService.sendMemberBannedNotification(
                fanHubMember.getUser().getId(),
                fanHubId,
                fanHubMember.getHub().getHubName(),
                request.getReason(),
                request.getBannedUntil(),
                banType
        );

        return "Member banned successfully";
    }

    @Override
    public void checkBanStatus(Long hubId, Long userId, List<String> banTypes) {
        List<BanMember> activeBans = banMemberRepository
                .findByHubIdAndUserIdAndIsActiveTrueAndBanTypeIn(hubId, userId, banTypes);

        if (!activeBans.isEmpty()) {
            BanMember ban = activeBans.get(0);
            
            String durationStr = "permanent";
            if (ban.getBannedUntil() != null) {
                java.time.Duration duration = java.time.Duration.between(Instant.now(), ban.getBannedUntil());
                long days = duration.toDays();
                long hours = duration.toHoursPart();
                long minutes = duration.toMinutesPart();
                
                StringBuilder sb = new StringBuilder();
                if (days > 0) sb.append(days).append("d ");
                if (hours > 0) sb.append(hours).append("h ");
                if (minutes > 0) sb.append(minutes).append("m");
                durationStr = sb.toString().trim();
                if (durationStr.isEmpty()) durationStr = "less than a minute";
            }

            String action = "restricted from " + (ban.getBanType() != null ? ban.getBanType().toLowerCase() : "interacting");
            if ("COMMENT".equalsIgnoreCase(ban.getBanType())) {
                action = "Restricted from commenting from this hub";
            } else if ("POST".equalsIgnoreCase(ban.getBanType())) {
                action = "Restricted from creating post from this hub";
            } else if ("JOIN".equalsIgnoreCase(ban.getBanType())) {
                action = "Banned from joining this hub";
            }

            String message = String.format("You are %s. Duration remaining: %s",
                    action, durationStr);
            throw new AccessDeniedException(message);
        }
    }

    @Override
    public List<BanMemberResponse> getActiveBansByHubId(Long fanHubId, int pageNo, int pageSize, String sortBy, String sortDir, String banType) {
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

        Sort sort = sortDir.equalsIgnoreCase("desc") ? Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
        Pageable paging = PageRequest.of(pageNo, pageSize, sort);
        Page<BanMember> pagedBans;
        
        if (banType != null && !banType.isEmpty()) {
            pagedBans = banMemberRepository.findByHubIdAndIsActiveTrueAndBanType(fanHubId, banType.toUpperCase(), paging);
        } else {
            pagedBans = banMemberRepository.findByHubIdAndIsActiveTrue(fanHubId, paging);
        }

        if (pagedBans.hasContent()) {
            return pagedBans.getContent().stream()
                    .map(this::mapToBanMemberResponse)
                    .collect(Collectors.toList());
        }

        return List.of();
    }

    @Override
    @org.springframework.transaction.annotation.Transactional
    public String revokeBan(Long banId) {
        User currentUser = authService.getUserFromToken(httpServletRequest);

        Optional<BanMember> banMember = banMemberRepository.findById(banId);
        if (banMember.isEmpty()) {
            throw new NotFoundException("Ban record not found");
        }

        Long fanHubId = banMember.get().getHub().getId();

        // Check if user is VTUBER and owns this FanHub
        boolean isOwner = "VTUBER".equals(currentUser.getRole()) &&
                fanHubRepository.findById(fanHubId)
                        .map(hub -> hub.getOwnerUser().getId().equals(currentUser.getId()))
                        .orElse(false);

        // Check if user is a member with MODERATOR role
        boolean isModerator = fanHubMemberRepository.findByHubIdAndUserId(fanHubId, currentUser.getId())
                .map(member -> "MODERATOR".equals(member.getRoleInHub()))
                .orElse(false);

        if (!isOwner && !isModerator) {
            throw new AccessDeniedException("Access denied");
        }

        // Check if ban is already inactive
        if (!banMember.get().getIsActive()) {
            throw new IllegalArgumentException("Ban is already inactive");
        }

        // Revoke the ban by setting isActive to false
        banMember.get().setIsActive(false);
        banMemberRepository.save(banMember.get());

        return "Ban revoked successfully";
    }

    private BanMemberResponse mapToBanMemberResponse(BanMember banMember) {
        BanMemberResponse response = new BanMemberResponse();
        response.setBanId(banMember.getId());
        response.setFanHubId(banMember.getHub().getId());
        response.setFanHubName(banMember.getHub().getHubName());
        response.setUserId(banMember.getUser().getId());
        response.setUsername(banMember.getUser().getUsername());
        response.setDisplayName(banMember.getUser().getDisplayName());
        response.setBannedByUserId(banMember.getBannedBy().getId());
        response.setBannedByUsername(banMember.getBannedBy().getUsername());
        response.setBannedByDisplayName(banMember.getBannedBy().getDisplayName());
        response.setReason(banMember.getReason());
        response.setBanType(banMember.getBanType());
        response.setBannedUntil(banMember.getBannedUntil());
        response.setIsActive(banMember.getIsActive());
        response.setCreatedAt(banMember.getCreatedAt());
        return response;
    }

    /**
     * Runs daily at midnight (00:00) UTC+7 to deactivate expired bans.
     */
    @Override
    @Scheduled(cron = "0 0 0 * * *", zone = "Asia/Ho_Chi_Minh")
    @org.springframework.transaction.annotation.Transactional
    public void deactivateExpiredBans() {
        Instant now = Instant.now();
        List<BanMember> expiredBans = banMemberRepository.findExpiredBans(now);

        if (!expiredBans.isEmpty()) {
            log.info("Deactivating {} expired bans", expiredBans.size());
            for (BanMember ban : expiredBans) {
                ban.setIsActive(false);
            }
            banMemberRepository.saveAll(expiredBans);
            log.info("Successfully deactivated {} expired bans", expiredBans.size());
        }
    }

    @Override
    public List<MemberWithBansResponse> getAllMembersWithBans(Long fanHubId, int pageNo, int pageSize, String sortBy, String sortDir) {
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

        Sort sort = sortDir.equalsIgnoreCase("desc") ? Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
        Pageable paging = PageRequest.of(pageNo, pageSize, sort);
        Page<BanMember> pagedBans = banMemberRepository.findByHubId(fanHubId, paging);

        if (pagedBans.isEmpty()) {
            return List.of();
        }

        // Group bans by user
        Map<User, List<BanMember>> userToBansMap = pagedBans.getContent().stream()
                .collect(Collectors.groupingBy(BanMember::getUser));

        return userToBansMap.entrySet().stream()
                .map(entry -> {
                    User user = entry.getKey();
                    List<BanMember> bans = entry.getValue();
                    Optional<FanHubMember> fanHubMember = fanHubMemberRepository.findByHubIdAndUserId(fanHubId, user.getId());
                    return mapToMemberWithBansResponse(user, fanHubMember, fanHub.get(), bans);
                })
                .collect(Collectors.toList());
    }

    private MemberWithBansResponse mapToMemberWithBansResponse(User user, Optional<FanHubMember> fanHubMemberOpt, FanHub fanHub, List<BanMember> bans) {
        MemberWithBansResponse response = new MemberWithBansResponse();

        response.setUserId(user.getId());
        response.setUsername(user.getUsername());
        response.setDisplayName(user.getDisplayName());
        response.setAvatarUrl(user.getAvatarUrl());
        response.setFrameUrl(user.getFrameUrl());

        if (fanHubMemberOpt.isPresent()) {
            FanHubMember fanHubMember = fanHubMemberOpt.get();
            response.setMemberId(fanHubMember.getId());
            response.setRoleInHub(fanHubMember.getRoleInHub());
            response.setMemberStatus(fanHubMember.getStatus());
            response.setJoinedAt(fanHubMember.getJoinedAt());
        }

        response.setFanHubId(fanHub.getId());
        response.setFanHubName(fanHub.getHubName());
        response.setFanHubSubdomain(fanHub.getSubdomain());

        List<MemberWithBansResponse.SimpleMemberBanResponse> banResponses = bans.stream()
                .map(this::mapToSimpleMemberBanResponse)
                .collect(Collectors.toList());
        response.setBans(banResponses);

        return response;
    }

    private MemberWithBansResponse.SimpleMemberBanResponse mapToSimpleMemberBanResponse(BanMember banMember) {
        MemberWithBansResponse.SimpleMemberBanResponse response = new MemberWithBansResponse.SimpleMemberBanResponse();
        response.setBanId(banMember.getId());
        response.setBannedByUserId(banMember.getBannedBy().getId());
        response.setBannedByUsername(banMember.getBannedBy().getUsername());
        response.setBannedByDisplayName(banMember.getBannedBy().getDisplayName());
        response.setReason(banMember.getReason());
        response.setBanType(banMember.getBanType());
        response.setBannedUntil(banMember.getBannedUntil());
        response.setIsActive(banMember.getIsActive());
        response.setCreatedAt(banMember.getCreatedAt());
        return response;
    }
}
