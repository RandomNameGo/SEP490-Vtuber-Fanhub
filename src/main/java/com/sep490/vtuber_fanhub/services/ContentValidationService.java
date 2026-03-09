package com.sep490.vtuber_fanhub.services;

import org.springframework.web.multipart.MultipartFile;

public interface ContentValidationService {
    String validateText(String text);
    String validateImageFile(MultipartFile file);
    String validateImageUrl(String url);
}
