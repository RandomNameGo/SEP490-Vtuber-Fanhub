package com.sep490.vtuber_fanhub.services;

import com.sep490.vtuber_fanhub.dto.requests.FanHubJoinAnswerRequest;
import com.sep490.vtuber_fanhub.dto.responses.FanHubJoinAnswerResponse;

import java.util.List;

public interface FanHubJoinAnswerService {
    String submitAnswers(Long hubId, List<FanHubJoinAnswerRequest> answers);
    List<FanHubJoinAnswerResponse> getAnswersByMemberId(Long memberId);
}
