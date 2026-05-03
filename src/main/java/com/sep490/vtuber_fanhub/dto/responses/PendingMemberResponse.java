package com.sep490.vtuber_fanhub.dto.responses;

import lombok.Data;
import java.time.Instant;
import java.util.List;

@Data
public class PendingMemberResponse {
    private Long id;
    private Long hubId;
    private String hubName;
    private Long userId;
    private String username;
    private String displayName;
    private String avatarUrl;
    private String frameUrl;
    private java.math.BigDecimal frameSize;
    private java.math.BigDecimal frameXAxis;
    private java.math.BigDecimal frameYAxis;
    private String roleInHub;
    private String status;
    private Integer fanHubScore;
    private Instant joinedAt;
    private String title;

    private List<FanHubJoinAnswerResponse> joinAnswers;
}
