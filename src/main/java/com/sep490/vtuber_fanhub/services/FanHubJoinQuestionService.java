package com.sep490.vtuber_fanhub.services;

import com.sep490.vtuber_fanhub.dto.requests.FanHubJoinQuestionRequest;
import com.sep490.vtuber_fanhub.dto.responses.FanHubJoinQuestionResponse;

import java.util.List;

public interface FanHubJoinQuestionService {
    List<FanHubJoinQuestionResponse> getQuestionsByHubId(Long hubId);
    String createQuestion(Long hubId, FanHubJoinQuestionRequest request);
    String updateQuestion(Long questionId, FanHubJoinQuestionRequest request);
    String deleteQuestion(Long questionId);
}
