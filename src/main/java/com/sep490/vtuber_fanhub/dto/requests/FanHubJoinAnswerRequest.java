package com.sep490.vtuber_fanhub.dto.requests;

import lombok.Data;

@Data
public class FanHubJoinAnswerRequest {
    private Long questionId;
    private String content;
}
