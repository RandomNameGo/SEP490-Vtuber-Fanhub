package com.sep490.vtuber_fanhub.services;

import com.sep490.vtuber_fanhub.dto.responses.FanHubModelResponse;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

public interface FanHubModelService {
    FanHubModelResponse uploadModel(Long fanHubId, String name, MultipartFile modelFile,
                                  List<MultipartFile> spriteFiles, List<String> spriteNames,
                                  List<Integer> spriteFrames) throws IOException;
    FanHubModelResponse getModelByFanHubId(Long fanHubId);
}
