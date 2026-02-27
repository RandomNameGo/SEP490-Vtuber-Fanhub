package com.sep490.vtuber_fanhub.services;

import com.sep490.vtuber_fanhub.dto.responses.FanHubMemberResponse;

import java.util.List;

public interface FanHubMemberService {

    String joinFanHubMember(long fanHubId);

    List<FanHubMemberResponse> getFanHubMembers(long fanHubId, int pageNo, int pageSize, String sortBy);

    List<FanHubMemberResponse> getPendingFanHubMembers(long fanHubId, int pageNo, int pageSize, String sortBy);

    String addModerator(long fanHubId, List<Long> fanHubMemberIds);
}
