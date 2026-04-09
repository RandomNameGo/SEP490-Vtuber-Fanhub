package com.sep490.vtuber_fanhub.services;

import com.fasterxml.jackson.databind.JsonNode;
import com.google.genai.types.GenerateContentResponse;
import com.sep490.vtuber_fanhub.dto.responses.AIMessageResponse;
import com.sep490.vtuber_fanhub.models.Enum.ChatPersonalityType;

public interface GeminiAIService {
    String test();
    AIMessageResponse sendPrompt(String prompt, ChatPersonalityType type);
    JsonNode listModels();
    GenerateContentResponse sendPromptFullResponse(String prompt, ChatPersonalityType type);
    String translateText(String text, String language);
    String summarizeText(String text, String language);

    // these are for function calling of chatbot, which require userid for authentication
    AIMessageResponse sendPromptFunctionCalling(String prompt, ChatPersonalityType type, Long userId);
    GenerateContentResponse sendPromptFunctionCallingFullResponse(String prompt, ChatPersonalityType type, Long userId);
}
