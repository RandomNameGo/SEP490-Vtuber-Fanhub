package com.sep490.vtuber_fanhub.services;

import com.sep490.vtuber_fanhub.dto.requests.CreateReportMemberRequest;

public interface ReportMemberService {

    String createReportMember(CreateReportMemberRequest createReportMemberRequest);
}
