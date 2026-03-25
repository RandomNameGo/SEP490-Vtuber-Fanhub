package com.sep490.vtuber_fanhub.services;

import com.fasterxml.jackson.databind.JsonNode;
import com.google.genai.types.GenerateContentResponse;
import com.sep490.vtuber_fanhub.models.Enum.ChatPersonalityType;

public interface GeminiAIService {
    String test();
    String sendPrompt(String prompt, ChatPersonalityType type);
    JsonNode listModels();
    GenerateContentResponse sendPromptFullResponse(String prompt, ChatPersonalityType type);
    String translateText(String text, String language);

}
