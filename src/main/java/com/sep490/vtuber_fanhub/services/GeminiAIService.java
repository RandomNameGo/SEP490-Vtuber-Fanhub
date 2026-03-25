package com.sep490.vtuber_fanhub.services;

import com.fasterxml.jackson.databind.JsonNode;
import com.google.genai.types.GenerateContentResponse;

public interface GeminiAIService {
    String test();
    String sendPrompt(String prompt);
    JsonNode listModels();
    GenerateContentResponse sendPromptFullResponse(String prompt);
}
