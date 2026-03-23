package com.sep490.vtuber_fanhub.services;

import com.sep490.vtuber_fanhub.dto.requests.CreateBanMemberRequest;
import com.sep490.vtuber_fanhub.dto.responses.BanMemberResponse;
import com.sep490.vtuber_fanhub.exceptions.NotFoundException;
import com.sep490.vtuber_fanhub.models.BanMember;
import com.sep490.vtuber_fanhub.models.FanHubMember;
import com.sep490.vtuber_fanhub.models.User;
import com.sep490.vtuber_fanhub.repositories.BanMemberRepository;
import com.sep490.vtuber_fanhub.repositories.FanHubMemberRepository;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Service
@RequiredArgsConstructor
public class BanMemberServiceImpl implements BanMemberService {

    private final BanMemberRepository banMemberRepository;

    private final FanHubMemberRepository fanHubMemberRepository;

    private final AuthService authService;

    private final HttpServletRequest httpServletRequest;

    @Override
    public String banFanHubMember(CreateBanMemberRequest request) {

        User currentUser = authService.getUserFromToken(httpServletRequest);

        FanHubMember fanHubMember = fanHubMemberRepository.findById(request.getFanHubMemberId())
                .orElseThrow(() -> new NotFoundException("Fan hub member not found"));

        BanMember banMember = new BanMember();
        banMember.setHub(fanHubMember.getHub());
        banMember.setUser(fanHubMember.getUser());
        banMember.setBannedBy(currentUser);
        banMember.setReason(request.getReason());
        banMember.setBanType(request.getBanType());
        banMember.setBannedUntil(request.getBannedUntil());
        banMember.setIsActive(true);
        banMember.setCreatedAt(Instant.now());
        banMemberRepository.save(banMember);

        return "Member banned successfully";
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
}
