package com.sep490.vtuber_fanhub.services;

import com.sep490.vtuber_fanhub.dto.requests.CreateSystemAccountRequest;
import com.sep490.vtuber_fanhub.dto.requests.CreateVTuberApplication;

public interface VTuberApplicationService {
    String createVTuberApplication(CreateVTuberApplication request);
}
