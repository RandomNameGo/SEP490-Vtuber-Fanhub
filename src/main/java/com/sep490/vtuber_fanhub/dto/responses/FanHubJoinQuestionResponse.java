package com.sep490.vtuber_fanhub.dto.responses;

import lombok.Data;
import java.time.Instant;

@Data
public class FanHubJoinQuestionResponse {
    private Long id;
    private Long hubId;
    private String content;
    private Integer orderNumber;
    private Instant createdAt;
}
