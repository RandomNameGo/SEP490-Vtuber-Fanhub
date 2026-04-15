package com.sep490.vtuber_fanhub.dto.responses;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class FanHubAnalyticsResponse {
    private long totalJoinedMembers;
    private long totalPosts;
    private List<FanHubMemberResponse> topMembers;
}
