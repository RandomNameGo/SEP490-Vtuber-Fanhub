package com.sep490.vtuber_fanhub.dto.requests;

import lombok.Data;

@Data
public class UpdateJoinAnswerRequest {
    private Long answerId;
    private String content;
}
