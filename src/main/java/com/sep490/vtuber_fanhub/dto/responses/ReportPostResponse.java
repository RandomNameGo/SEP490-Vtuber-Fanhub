package com.sep490.vtuber_fanhub.dto.responses;

import lombok.Data;

import java.time.Instant;

@Data
public class ReportPostResponse {

    private Long reportId;

    private Long postId;
    private Long fanHubId;
    private String postTitle;

    private Long reportedByUserId;
    private String reportedByUsername;
    private String reportedByDisplayName;

    private String reason;
    private String status;

    private Instant createdAt;
}
