package com.sep490.vtuber_fanhub.services;

import com.sep490.vtuber_fanhub.dto.responses.FanHubMemberResponse;
import com.sep490.vtuber_fanhub.dto.responses.MemberDetailResponse;

import java.util.List;

public interface FanHubMemberService {

    String joinFanHubMember(long fanHubId);

    List<FanHubMemberResponse> getFanHubMembers(long fanHubId, int pageNo, int pageSize, String sortBy);

    List<FanHubMemberResponse> getPendingFanHubMembers(long fanHubId, int pageNo, int pageSize, String sortBy);

    String addModerator(long fanHubId, List<Long> fanHubMemberIds);

    String reviewFanHubMember(long fanHubMemberId, String status);

    MemberDetailResponse getMemberDetail(long fanHubMemberId);
}
