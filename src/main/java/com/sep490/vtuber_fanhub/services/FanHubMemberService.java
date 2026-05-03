package com.sep490.vtuber_fanhub.services;

import com.sep490.vtuber_fanhub.dto.requests.FanHubJoinAnswerRequest;
import com.sep490.vtuber_fanhub.dto.requests.UpdateJoinAnswerRequest;
import com.sep490.vtuber_fanhub.dto.responses.*;

import java.util.List;

public interface FanHubMemberService {

    String joinFanHubMember(long fanHubId);

    String joinFanHubMemberWithAnswers(long fanHubId, List<FanHubJoinAnswerRequest> answers);

    List<FanHubMemberResponse> getFanHubMembers(long fanHubId, int pageNo, int pageSize, String sortBy, String sortDir, String username, String role);

    List<PendingMemberResponse> getPendingFanHubMembers(long fanHubId, int pageNo, int pageSize, String sortBy, String sortDir);

    String addModerator(long fanHubId, List<Long> fanHubMemberIds);

    String removeModerator(long fanHubId, List<Long> fanHubMemberIds);

    String reviewFanHubMember(long fanHubMemberId, String status);

    MemberDetailResponse getMemberDetail(long fanHubMemberId);

    FanHubMembershipResponse checkUserMembership(Long fanHubId);

    FanHubMembershipResponse checkUserMembershipAnyStatus(Long fanHubId);

    Boolean checkUserSentJoinRequest(Long fanHubId);

    String updateJoinAnswers(List<UpdateJoinAnswerRequest> requests);

    List<UserFanHubAnswersResponse> getMyJoinAnswers(Long fanHubId);

    String deleteJoinRequest(long fanHubId);

    String leaveFanHub(long fanHubId);

    String kickMember(long fanHubId, long memberId);

    long countPendingMembersByFanHubId(Long fanHubId);
}
