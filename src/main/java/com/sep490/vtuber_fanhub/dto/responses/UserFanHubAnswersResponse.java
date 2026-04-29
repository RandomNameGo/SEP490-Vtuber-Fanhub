package com.sep490.vtuber_fanhub.dto.responses;

import lombok.Data;
import java.util.List;

@Data
public class UserFanHubAnswersResponse {
    private Long fanHubId;
    private String fanHubName;
    private List<FanHubJoinAnswerResponse> answers;
}
