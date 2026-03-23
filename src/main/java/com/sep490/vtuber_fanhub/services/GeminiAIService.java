package com.sep490.vtuber_fanhub.services;

import com.fasterxml.jackson.databind.JsonNode;

public interface GeminiAIService {
    String test();
    String sendPrompt(String prompt);
    JsonNode listModels();
}
