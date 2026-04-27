package com.sep490.vtuber_fanhub.dto.requests;

import lombok.Data;

@Data
public class FanHubJoinQuestionRequest {
    private String content;
    private Integer orderNumber;
}
