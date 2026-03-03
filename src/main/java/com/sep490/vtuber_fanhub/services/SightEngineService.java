package com.sep490.vtuber_fanhub.services;

import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.web.multipart.MultipartFile;

public interface SightEngineService {
    JsonNode checkImage(MultipartFile file);
}
