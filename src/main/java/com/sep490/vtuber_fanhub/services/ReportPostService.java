package com.sep490.vtuber_fanhub.services;

import com.sep490.vtuber_fanhub.dto.requests.CreateReportPostRequest;
import com.sep490.vtuber_fanhub.models.ReportPost;

public interface ReportPostService {

    String createReportPost(CreateReportPostRequest createReportPostRequest);
}
