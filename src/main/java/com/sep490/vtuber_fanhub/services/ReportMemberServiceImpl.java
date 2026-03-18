package com.sep490.vtuber_fanhub.services;

import com.sep490.vtuber_fanhub.dto.requests.CreateReportMemberRequest;
import com.sep490.vtuber_fanhub.exceptions.NotFoundException;
import com.sep490.vtuber_fanhub.models.FanHubMember;
import com.sep490.vtuber_fanhub.models.ReportMember;
import com.sep490.vtuber_fanhub.models.User;
import com.sep490.vtuber_fanhub.repositories.FanHubMemberRepository;
import com.sep490.vtuber_fanhub.repositories.ReportMemberRepository;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ReportMemberServiceImpl implements ReportMemberService {

    private final ReportMemberRepository reportMemberRepository;

    private final AuthService authService;

    private final HttpServletRequest httpServletRequest;

    private final FanHubMemberRepository fanHubMemberRepository;

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
}
