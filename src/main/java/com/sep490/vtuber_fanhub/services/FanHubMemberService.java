package com.sep490.vtuber_fanhub.services;

import com.sep490.vtuber_fanhub.dto.responses.FanHubMemberResponse;
import com.sep490.vtuber_fanhub.dto.responses.FanHubMembershipResponse;
import com.sep490.vtuber_fanhub.dto.responses.MemberDetailResponse;
import com.sep490.vtuber_fanhub.dto.responses.PendingMemberResponse;

import java.util.List;

public interface FanHubMemberService {

    String joinFanHubMember(long fanHubId);

    String joinFanHubMemberWithAnswers(long fanHubId, List<com.sep490.vtuber_fanhub.dto.requests.FanHubJoinAnswerRequest> answers);

    List<FanHubMemberResponse> getFanHubMembers(long fanHubId, int pageNo, int pageSize, String sortBy, String username, String role);

    List<PendingMemberResponse> getPendingFanHubMembers(long fanHubId, int pageNo, int pageSize, String sortBy);

    String addModerator(long fanHubId, List<Long> fanHubMemberIds);

    String removeModerator(long fanHubId, List<Long> fanHubMemberIds);

    String reviewFanHubMember(long fanHubMemberId, String status);

    MemberDetailResponse getMemberDetail(long fanHubMemberId);

    FanHubMembershipResponse checkUserMembership(Long fanHubId);

    String leaveFanHub(long fanHubId);

    String kickMember(long fanHubId, long memberId);
}
