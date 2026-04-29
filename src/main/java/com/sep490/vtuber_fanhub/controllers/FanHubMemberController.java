package com.sep490.vtuber_fanhub.controllers;

import com.sep490.vtuber_fanhub.dto.requests.CreateBanMemberRequest;
import com.sep490.vtuber_fanhub.dto.requests.CreateReportMemberRequest;
import com.sep490.vtuber_fanhub.dto.requests.FanHubJoinAnswerRequest;
import com.sep490.vtuber_fanhub.dto.requests.UpdateJoinAnswerRequest;
import com.sep490.vtuber_fanhub.dto.responses.*;
import com.sep490.vtuber_fanhub.services.BanMemberService;
import com.sep490.vtuber_fanhub.services.FanHubMemberService;
import com.sep490.vtuber_fanhub.services.ReportMemberService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("vhub/api/v1/fan-hub-member")
@RequiredArgsConstructor
public class FanHubMemberController {

    private final FanHubMemberService fanHubMemberService;

    private final ReportMemberService reportMemberService;

    private final BanMemberService banMemberService;

    @PostMapping("/join/{fanHubId}")
    @PreAuthorize("hasAnyRole('USER', 'VTUBER')")
    public ResponseEntity<?> joinFanHub(@PathVariable long fanHubId) {
        return ResponseEntity.ok().body(APIResponse.<String>builder()
                .success(true)
                .message("Success")
                .data(fanHubMemberService.joinFanHubMember(fanHubId))
                .build()
        );
    }

    @PostMapping("/join-with-answers/{fanHubId}")
    @PreAuthorize("hasAnyRole('USER', 'VTUBER')")
    public ResponseEntity<?> joinFanHubWithAnswers(
            @PathVariable long fanHubId,
            @RequestBody List<FanHubJoinAnswerRequest> answers) {
        return ResponseEntity.ok().body(APIResponse.<String>builder()
                .success(true)
                .message("Success")
                .data(fanHubMemberService.joinFanHubMemberWithAnswers(fanHubId, answers))
                .build()
        );
    }

    @GetMapping("/members/{fanHubId}")
    @PreAuthorize("hasAnyRole('USER', 'VTUBER')")
    public ResponseEntity<?> getFanHubMembers(
            @PathVariable long fanHubId,
            @RequestParam(defaultValue = "0") int pageNo,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(defaultValue = "joinedAt") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir,
            @RequestParam(required = false) String username,
            @RequestParam(required = false) String role) {
        return ResponseEntity.ok().body(APIResponse.<List<FanHubMemberResponse>>builder()
                .success(true)
                .message("Success")
                .data(fanHubMemberService.getFanHubMembers(fanHubId, pageNo, pageSize, sortBy, sortDir, username, role))
                .build()
        );
    }

    @GetMapping("/pending-members/{fanHubId}")
    @PreAuthorize("hasAnyRole('VTUBER', 'USER')")
    public ResponseEntity<?> getPendingFanHubMembers(
            @PathVariable long fanHubId,
            @RequestParam(defaultValue = "0") int pageNo,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(defaultValue = "joinedAt") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir) {
        return ResponseEntity.ok().body(APIResponse.<List<com.sep490.vtuber_fanhub.dto.responses.PendingMemberResponse>>builder()
                .success(true)
                .message("Success")
                .data(fanHubMemberService.getPendingFanHubMembers(fanHubId, pageNo, pageSize, sortBy, sortDir))
                .build()
        );
    }

    @PostMapping("/set-moderator/{fanHubId}")
    @PreAuthorize("hasRole('VTUBER')")
    public ResponseEntity<?> setModerator(@PathVariable long fanHubId, @RequestParam List<Long> memberIds) {
        return ResponseEntity.ok().body(APIResponse.<String>builder()
                .success(true)
                .message("Success")
                .data(fanHubMemberService.addModerator(fanHubId, memberIds))
                .build()
        );
    }

    @PostMapping("/remove-moderator/{fanHubId}")
    @PreAuthorize("hasRole('VTUBER')")
    public ResponseEntity<?> removeModerator(@PathVariable long fanHubId, @RequestParam List<Long> memberIds) {
        return ResponseEntity.ok().body(APIResponse.<String>builder()
                .success(true)
                .message("Success")
                .data(fanHubMemberService.removeModerator(fanHubId, memberIds))
                .build()
        );
    }

    @PutMapping("/review")
    @PreAuthorize("hasAnyRole('VTUBER', 'MODERATOR')")
    public ResponseEntity<?> reviewFanHubMember(
            @RequestParam long fanHubMemberId,
            @RequestParam String status) {
        return ResponseEntity.ok().body(APIResponse.<String>builder()
                .success(true)
                .message("Success")
                .data(fanHubMemberService.reviewFanHubMember(fanHubMemberId, status))
                .build()
        );
    }

    @PostMapping("/report")
    @PreAuthorize("hasAnyRole('USER', 'VTUBER')")
    public ResponseEntity<?> reportMember(@RequestBody CreateReportMemberRequest createReportMemberRequest) {
        return ResponseEntity.ok().body(APIResponse.<String>builder()
                .success(true)
                .message("Success")
                .data(reportMemberService.createReportMember(createReportMemberRequest))
                .build()
        );
    }

    @GetMapping("/reports/members/{fanHubId}")
    @PreAuthorize("hasAnyRole('USER', 'VTUBER')")
    public ResponseEntity<?> getReportMembersByFanHubId(
            @PathVariable Long fanHubId,
            @RequestParam(defaultValue = "0") int pageNo,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {

        return ResponseEntity.ok().body(APIResponse.<List<ReportMemberResponse>>builder()
                .success(true)
                .message("Success")
                .data(reportMemberService.getReportMembersByFanHubId(fanHubId, pageNo, pageSize, sortBy, sortDir))
                .build()
        );
    }

    @PutMapping("/report/resolve")
    @PreAuthorize("hasAnyRole('USER', 'VTUBER')")
    public ResponseEntity<?> resolveReportMember(
            @RequestParam Long reportId,
            @RequestParam(required = false) String resolveMessage) {
        return ResponseEntity.ok().body(APIResponse.<String>builder()
                .success(true)
                .message("Success")
                .data(reportMemberService.resolveReportMember(reportId, resolveMessage))
                .build()
        );
    }

    @PostMapping("/ban")
    @PreAuthorize("hasAnyRole('USER', 'VTUBER')")
    public ResponseEntity<?> banMember(@RequestBody CreateBanMemberRequest request) {
        return ResponseEntity.ok().body(APIResponse.<String>builder()
                .success(true)
                .message("Success")
                .data(banMemberService.banFanHubMember(request))
                .build()
        );
    }

    @GetMapping("/bans/{fanHubId}")
    @PreAuthorize("hasAnyRole('USER', 'VTUBER')")
    public ResponseEntity<?> getActiveBansByHubId(
            @PathVariable Long fanHubId,
            @RequestParam(defaultValue = "0") int pageNo,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir,
            @RequestParam(required = false) String banType) {

        return ResponseEntity.ok().body(APIResponse.<List<BanMemberResponse>>builder()
                .success(true)
                .message("Success")
                .data(banMemberService.getActiveBansByHubId(fanHubId, pageNo, pageSize, sortBy, sortDir, banType))
                .build()
        );
    }

    @PutMapping("/ban/revoke")
    @PreAuthorize("hasAnyRole('USER', 'VTUBER')")
    public ResponseEntity<?> revokeBan(@RequestParam Long banId) {
        return ResponseEntity.ok().body(APIResponse.<String>builder()
                .success(true)
                .message("Success")
                .data(banMemberService.revokeBan(banId))
                .build()
        );
    }

    @PutMapping("/{fanHubId}/leave")
    @PreAuthorize("hasAnyRole('USER', 'VTUBER')")
    public ResponseEntity<?> leaveFanHub(@PathVariable long fanHubId) {
        return ResponseEntity.ok().body(APIResponse.<String>builder()
                .success(true)
                .message("Success")
                .data(fanHubMemberService.leaveFanHub(fanHubId))
                .build()
        );
    }

    @PutMapping("/{fanHubId}/kick/{memberId}")
    @PreAuthorize("hasAnyRole('USER', 'VTUBER')")
    public ResponseEntity<?> kickMember(@PathVariable long fanHubId, @PathVariable long memberId) {
        return ResponseEntity.ok().body(APIResponse.<String>builder()
                .success(true)
                .message("Success")
                .data(fanHubMemberService.kickMember(fanHubId, memberId))
                .build()
        );
    }

    @GetMapping("/members/{fanHubMemberId}/detail")
    @PreAuthorize("hasAnyRole('VTUBER', 'USER')")
    public ResponseEntity<?> getMemberDetail(@PathVariable long fanHubMemberId) {
        return ResponseEntity.ok().body(APIResponse.<MemberDetailResponse>builder()
                .success(true)
                .message("Success")
                .data(fanHubMemberService.getMemberDetail(fanHubMemberId))
                .build()
        );
    }


    @GetMapping("/{fanHubId}/is-member")
    public ResponseEntity<?> checkIsUserMemberOfFanHub(@PathVariable Long fanHubId) {
        FanHubMembershipResponse membership = fanHubMemberService.checkUserMembership(fanHubId);
        return ResponseEntity.ok().body(APIResponse.<FanHubMembershipResponse>builder()
                .success(true)
                .message("Success")
                .data(membership)
                .build()
        );
    }

    @GetMapping("/{fanHubId}/membership")
    public ResponseEntity<?> checkUserMembershipAnyStatus(@PathVariable Long fanHubId) {
        FanHubMembershipResponse membership = fanHubMemberService.checkUserMembershipAnyStatus(fanHubId);
        return ResponseEntity.ok().body(APIResponse.<FanHubMembershipResponse>builder()
                .success(true)
                .message("Success")
                .data(membership)
                .build()
        );
    }

    @GetMapping("/{fanHubId}/check-join-request")
    public ResponseEntity<?> checkIsPending(@PathVariable Long fanHubId) {
        return ResponseEntity.ok().body(APIResponse.<Boolean>builder()
                .success(true)
                .message("Success")
                .data(fanHubMemberService.checkUserSentJoinRequest(fanHubId))
                .build()
        );
    }

    @PutMapping("/answers")
    @PreAuthorize("hasAnyRole('USER', 'VTUBER')")
    public ResponseEntity<?> updateJoinAnswers(@RequestBody List<UpdateJoinAnswerRequest> requests) {
        return ResponseEntity.ok().body(APIResponse.<String>builder()
                .success(true)
                .message("Success")
                .data(fanHubMemberService.updateJoinAnswers(requests))
                .build()
        );
    }

    @GetMapping("/my-answers")
    @PreAuthorize("hasAnyRole('USER', 'VTUBER')")
    public ResponseEntity<?> getMyJoinAnswers(@RequestParam(required = false) Long fanHubId) {
        return ResponseEntity.ok().body(APIResponse.<List<UserFanHubAnswersResponse>>builder()
                .success(true)
                .message("Success")
                .data(fanHubMemberService.getMyJoinAnswers(fanHubId))
                .build()
        );
    }

    @DeleteMapping("/{fanHubId}/join-request")
    @PreAuthorize("hasAnyRole('USER', 'VTUBER')")
    public ResponseEntity<?> deleteJoinRequest(@PathVariable long fanHubId) {
        return ResponseEntity.ok().body(APIResponse.<String>builder()
                .success(true)
                .message("Success")
                .data(fanHubMemberService.deleteJoinRequest(fanHubId))
                .build()
        );
    }

    @GetMapping("/reports/my-members-report")
    @PreAuthorize("hasAnyRole('USER', 'VTUBER')")
    public ResponseEntity<?> getMyReportMembers(
            @RequestParam(defaultValue = "0") int pageNo,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {

        return ResponseEntity.ok().body(APIResponse.<List<ReportMemberResponse>>builder()
                .success(true)
                .message("Success")
                .data(reportMemberService.getReportMembersByCurrentUser(pageNo, pageSize, sortBy, sortDir))
                .build()
        );
    }

    @GetMapping("/reports/pending-members/{fanHubId}")
    @PreAuthorize("hasAnyRole('USER', 'VTUBER')")
    public ResponseEntity<?> getPendingReportMembers(
            @PathVariable Long fanHubId,
            @RequestParam(defaultValue = "0") int pageNo,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {

        return ResponseEntity.ok().body(APIResponse.<List<ReportMemberResponse>>builder()
                .success(true)
                .message("Success")
                .data(reportMemberService.getPendingReportMembersByFanHubId(fanHubId, pageNo, pageSize, sortBy, sortDir))
                .build()
        );
    }

    @PutMapping("/reports/bulk-resolve")
    @PreAuthorize("hasAnyRole('USER', 'VTUBER')")
    public ResponseEntity<?> bulkResolveReportMembers(
            @RequestParam List<Long> reportIds,
            @RequestParam(required = false) String resolveMessage) {

        return ResponseEntity.ok().body(APIResponse.<String>builder()
                .success(true)
                .message("Success")
                .data(reportMemberService.bulkResolveReportMembers(reportIds, resolveMessage))
                .build()
        );
    }

    @GetMapping("/reports/members-with-reports/{fanHubId}")
    @PreAuthorize("hasAnyRole('USER', 'VTUBER')")
    public ResponseEntity<?> getAllMembersWithReports(
            @PathVariable Long fanHubId,
            @RequestParam(defaultValue = "0") int pageNo,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {

        return ResponseEntity.ok().body(APIResponse.<List<MemberWithReportsResponse>>builder()
                .success(true)
                .message("Success")
                .data(reportMemberService.getAllMembersWithReports(fanHubId, pageNo, pageSize, sortBy, sortDir))
                .build()
        );
    }

    @GetMapping("/bans/members-with-bans/{fanHubId}")
    @PreAuthorize("hasAnyRole('USER', 'VTUBER')")
    public ResponseEntity<?> getAllMembersWithBans(
            @PathVariable Long fanHubId,
            @RequestParam(defaultValue = "0") int pageNo,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {

        return ResponseEntity.ok().body(APIResponse.<List<MemberWithBansResponse>>builder()
                .success(true)
                .message("Success")
                .data(banMemberService.getAllMembersWithBans(fanHubId, pageNo, pageSize, sortBy, sortDir))
                .build()
        );
    }
}
