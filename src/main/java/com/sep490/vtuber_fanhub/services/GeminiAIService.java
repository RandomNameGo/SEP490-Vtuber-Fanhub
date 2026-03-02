package com.sep490.vtuber_fanhub.services;

public interface GeminiAIService {
    String test();
    String sendPrompt(String prompt);
    String listModels();
}
