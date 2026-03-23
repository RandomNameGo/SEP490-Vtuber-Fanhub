package com.sep490.vtuber_fanhub.services;

import com.sep490.vtuber_fanhub.dto.requests.CreateBanMemberRequest;

public interface BanMemberService {

    String banFanHubMember(CreateBanMemberRequest request);
}
