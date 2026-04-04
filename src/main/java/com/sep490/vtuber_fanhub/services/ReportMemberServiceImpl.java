package com.sep490.vtuber_fanhub.services;

import com.sep490.vtuber_fanhub.dto.requests.CreateReportMemberRequest;
import com.sep490.vtuber_fanhub.dto.responses.ReportMemberResponse;
import com.sep490.vtuber_fanhub.exceptions.NotFoundException;
import com.sep490.vtuber_fanhub.models.FanHub;
import com.sep490.vtuber_fanhub.models.FanHubMember;
import com.sep490.vtuber_fanhub.models.ReportMember;
import com.sep490.vtuber_fanhub.models.User;
import com.sep490.vtuber_fanhub.repositories.FanHubMemberRepository;
import com.sep490.vtuber_fanhub.repositories.FanHubRepository;
import com.sep490.vtuber_fanhub.repositories.ReportMemberRepository;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ReportMemberServiceImpl implements ReportMemberService {

    private final ReportMemberRepository reportMemberRepository;

    private final AuthService authService;

    private final HttpServletRequest httpServletRequest;

    private final FanHubMemberRepository fanHubMemberRepository;

    private final FanHubRepository fanHubRepository;

    @Override
    public String createReportMember(CreateReportMemberRequest createReportMemberRequest) {

        User currentUser = authService.getUserFromToken(httpServletRequest);

        Optional<FanHubMember> fanHubMember = fanHubMemberRepository.findById(createReportMemberRequest.getMemberId());
        if (fanHubMember.isEmpty()) {
            throw new NotFoundException("Member not found");
        }

        ReportMember reportMember = new ReportMember();
        reportMember.setUser(fanHubMember.get().getUser());
        reportMember.setReportedBy(currentUser);
        reportMember.setHub(fanHubMember.get().getHub());
        reportMember.setReason(createReportMemberRequest.getReason());
        reportMember.setStatus("PENDING");
        reportMember.setCreatedAt(Instant.now());
        reportMemberRepository.save(reportMember);

        return "Report member sent successfully";
    }

    @Override
    public List<ReportMemberResponse> getReportMembersByFanHubId(Long fanHubId, int pageNo, int pageSize, String sortBy) {
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

        PageRequest pageRequest = PageRequest.of(pageNo, pageSize, Sort.by(Sort.Direction.DESC, sortBy));
        Page<ReportMember> reportMemberPage = reportMemberRepository.findByFanHubId(fanHubId, pageRequest);

        return reportMemberPage.getContent().stream()
                .map(this::mapToReportMemberResponse)
                .collect(Collectors.toList());
    }

    @Override
    public String resolveReportMember(Long reportId) {
        User currentUser = authService.getUserFromToken(httpServletRequest);

        Optional<ReportMember> reportMemberOpt = reportMemberRepository.findById(reportId);
        if (reportMemberOpt.isEmpty()) {
            throw new NotFoundException("Report not found");
        }

        ReportMember reportMember = reportMemberOpt.get();

        // Check if user is VTUBER and owns this FanHub
        boolean isOwner = "VTUBER".equals(currentUser.getRole()) &&
                reportMember.getHub().getOwnerUser().getId().equals(currentUser.getId());

        // Check if user is a member with MODERATOR role
        boolean isModerator = fanHubMemberRepository.findByHubIdAndUserId(reportMember.getHub().getId(), currentUser.getId())
                .map(member -> "MODERATOR".equals(member.getRoleInHub()))
                .orElse(false);

        if (!isOwner && !isModerator) {
            throw new AccessDeniedException("Access denied");
        }

        // If reported user is the current user, they cannot resolve their own report
        if (reportMember.getUser().getId().equals(currentUser.getId())) {
            throw new AccessDeniedException("Cannot resolve your own report");
        }

        reportMember.setStatus("RESOLVED");
        reportMemberRepository.save(reportMember);

        return "Report resolved successfully";
    }

    private ReportMemberResponse mapToReportMemberResponse(ReportMember reportMember) {
        ReportMemberResponse response = new ReportMemberResponse();
        response.setReportId(reportMember.getId());
        response.setReportedUserId(reportMember.getUser().getId());
        response.setReportedUsername(reportMember.getUser().getUsername());
        response.setReportedDisplayName(reportMember.getUser().getDisplayName());
        response.setFanHubId(reportMember.getHub().getId());
        response.setFanHubName(reportMember.getHub().getHubName());
        response.setReportedByUserId(reportMember.getReportedBy().getId());
        response.setReportedByUsername(reportMember.getReportedBy().getUsername());
        response.setReportedByDisplayName(reportMember.getReportedBy().getDisplayName());
        response.setReason(reportMember.getReason());
        response.setStatus(reportMember.getStatus());
        response.setCreatedAt(reportMember.getCreatedAt());
        return response;
    }
}
