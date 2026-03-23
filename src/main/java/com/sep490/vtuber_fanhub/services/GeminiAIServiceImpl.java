package com.sep490.vtuber_fanhub.services;

import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

@Service
public class GeminiAIServiceImpl implements  GeminiAIService{
    @Value("${google.api-key}")
    private String googleApiKey;

    private final String modelUrl = "https://ai.google.dev/gemini-api/docs/models/gemini-2.5-flash-lite#gemini-25-flash-lite:generateContent?key=";

    @Override
    public String test() {
        RestTemplate restTemplate = new RestTemplate();

        Map<String, Object> requestBody = Map.of(
                "contents", List.of(
                        Map.of("parts", List.of(
                                Map.of("text", "Say this is a test")
                        ))
                )
        );
        String outputText = "";
        try{
            Map<String, Object> response = restTemplate.postForObject(modelUrl + googleApiKey, requestBody, Map.class);
            outputText = extractTextFromResponse(response);
        }catch(Exception ermWhatTheSigma){
            ermWhatTheSigma.printStackTrace();
        }
        return outputText;
    }

    @Override
    public String sendPrompt(String prompt) {
        RestTemplate restTemplate = new RestTemplate();

        Map<String, Object> requestBody = Map.of(
                "contents", List.of(
                        Map.of("parts", List.of(
                                Map.of("text", prompt)
                        ))
                )
        );
        String outputText = "";
        try{
            Map<String, Object> response = restTemplate.postForObject(modelUrl + googleApiKey, requestBody, Map.class);
            outputText = extractTextFromResponse(response);
        }catch(Exception ermWhatTheSigma){
            ermWhatTheSigma.printStackTrace();
            throw new RuntimeException("gemini: Error sending prompt");
        }
        return outputText;
    }

    @Override
    public JsonNode listModels() {
        RestTemplate restTemplate = new RestTemplate();

        try {
            return restTemplate.getForObject("https://generativelanguage.googleapis.com/v1beta/models?key=" + googleApiKey, JsonNode.class);
        } catch (Exception e) {
            throw new RuntimeException("Error while fetching models: " + e.getMessage());
        }
    }

    private String extractTextFromResponse(Map<String, Object> response) {
        try {
            List<Map<String, Object>> candidates = (List<Map<String, Object>>) response.get("candidates");
            Map<String, Object> content = (Map<String, Object>) candidates.get(0).get("content");
            List<Map<String, Object>> parts = (List<Map<String, Object>>) content.get("parts");
            return (String) parts.get(0).get("text");
        } catch (Exception e) {
            return "Error parsing response: " + e.getMessage();
        }
    }
}
