package com.sep490.vtuber_fanhub.services;

import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.web.multipart.MultipartFile;

public interface ContentValidationService {
    String validateText(String text);
    String validateImageFile(MultipartFile file);
    String validateImageUrl(String url);
    String validateVideoUrl(String url);
    JsonNode validateVideoUrlAsync(String url);
    String handleCallbackResult(JsonNode result);
}
