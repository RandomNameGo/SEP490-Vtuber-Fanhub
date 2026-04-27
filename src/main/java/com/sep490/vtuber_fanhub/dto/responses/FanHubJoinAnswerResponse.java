package com.sep490.vtuber_fanhub.dto.responses;

import lombok.Data;

@Data
public class FanHubJoinAnswerResponse {
    private Long id;
    private Long questionId;
    private String questionContent;
    private String content;
}
