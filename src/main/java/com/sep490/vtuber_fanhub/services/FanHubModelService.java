package com.sep490.vtuber_fanhub.services;

import com.sep490.vtuber_fanhub.dto.responses.FanHubModelResponse;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

public interface FanHubModelService {
    FanHubModelResponse uploadModel(Long fanHubId, String name, MultipartFile modelFile, MultipartFile spriteFile) throws IOException;
    FanHubModelResponse getModelByFanHubId(Long fanHubId);
}
