package com.sep490.vtuber_fanhub.services;

import org.springframework.web.multipart.MultipartFile;

public interface SightEngineService {
    String checkImage(MultipartFile file);
}
