package com.sep490.vtuber_fanhub.controllers;

import com.sep490.vtuber_fanhub.dto.responses.APIResponse;
import com.sep490.vtuber_fanhub.dto.responses.FanHubMemberResponse;
import com.sep490.vtuber_fanhub.services.FanHubMemberService;
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

    @GetMapping("/members/{fanHubId}")
    @PreAuthorize("hasAnyRole('USER', 'VTUBER')")
    public ResponseEntity<?> getFanHubMembers(
            @PathVariable long fanHubId,
            @RequestParam(defaultValue = "0") int pageNo,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(defaultValue = "joinedAt") String sortBy) {
        return ResponseEntity.ok().body(APIResponse.<List<FanHubMemberResponse>>builder()
                .success(true)
                .message("Success")
                .data(fanHubMemberService.getFanHubMembers(fanHubId, pageNo, pageSize, sortBy))
                .build()
        );
    }

    @GetMapping("/pending-members/{fanHubId}")
    @PreAuthorize("hasAnyRole('USER', 'VTUBER')")
    public ResponseEntity<?> getPendingFanHubMembers(
            @PathVariable long fanHubId,
            @RequestParam(defaultValue = "0") int pageNo,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(defaultValue = "joinedAt") String sortBy) {
        return ResponseEntity.ok().body(APIResponse.<List<FanHubMemberResponse>>builder()
                .success(true)
                .message("Success")
                .data(fanHubMemberService.getPendingFanHubMembers(fanHubId, pageNo, pageSize, sortBy))
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

}
