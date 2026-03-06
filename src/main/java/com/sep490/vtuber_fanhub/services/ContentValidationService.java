package com.sep490.vtuber_fanhub.services;

import org.springframework.web.multipart.MultipartFile;

public interface ContentValidationService {
    String validateText(String text);
    String validateMediaFile(MultipartFile file);
    String validateMediaUrl(String url);
}
