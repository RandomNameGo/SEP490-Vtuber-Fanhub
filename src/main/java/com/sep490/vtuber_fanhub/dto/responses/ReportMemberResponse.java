package com.sep490.vtuber_fanhub.dto.responses;

import lombok.Data;

import java.time.Instant;

@Data
public class ReportMemberResponse {

    private Long reportId;

    private Long reportedUserId;
    private String reportedUsername;
    private String reportedDisplayName;

    private Long fanHubId;
    private String fanHubName;

    private Long reportedByUserId;
    private String reportedByUsername;
    private String reportedByDisplayName;

    private String reason;
    private String status;

    private Instant createdAt;
}
