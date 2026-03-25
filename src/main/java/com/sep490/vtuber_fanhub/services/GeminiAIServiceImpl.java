package com.sep490.vtuber_fanhub.services;

import com.fasterxml.jackson.databind.JsonNode;
import com.google.genai.Client;
import com.google.genai.types.*;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class GeminiAIServiceImpl implements GeminiAIService {

    @Value("${google.api-key}")
    private String googleApiKey;

    private Client client;
    private final String MODEL_ID = "gemini-2.5-flash-lite";

    private final FunctionCallingService functionCallingService;


    @PostConstruct
    public void init() {
        this.client = Client.builder()
                .apiKey(googleApiKey)
                .build();
    }

    @Override
    public String test() {
        return sendPrompt("Say this is a test");
    }

    @Override
    public String sendPrompt(String prompt) {
        GenerateContentResponse response = sendPromptFullResponse(prompt);
        return response.text();
    }

    @Override
    public GenerateContentResponse sendPromptFullResponse(String prompt) {
        try {
            // Define function declarations for the tools
            FunctionDeclaration getDisplayNameFunc = FunctionDeclaration.builder()
                    .name("get_display_name")
                    .description("Get the display name of the currently authenticated user")
                    .build();

            FunctionDeclaration testFunctionCallFunc = FunctionDeclaration.builder()
                    .name("test_function_call")
                    .description("A test function that returns a specific string")
                    .build();

            Tool tool = Tool.builder()
                    .functionDeclarations(List.of(getDisplayNameFunc, testFunctionCallFunc))
                    .build();

            GenerateContentConfig config = GenerateContentConfig.builder()
                    .temperature(1f)
                    .tools(tool)
                    .systemInstruction(Content.fromParts(Part.fromText
                            ("""
                                You are Matikanetannhauser from Uma Musume. talk like her.
                                
                                INSTRUCTIONS:
                                - Use the data from LAST_MESSAGES if there is available.
                                - Don't mention that you are analyzing previous messages.
                                - Don't say 'earlier you said' or 'based on the chat history'.
                                - Just answer naturally as if you already knew it.
                                - Be conversational and helpful
                                
                                """)))
                    .build();

            // Start the conversation with a list of content
            List<Content> contents = new ArrayList<>();
            contents.add(Content.fromParts(Part.fromText(prompt)));

            GenerateContentResponse response = client.models.generateContent(
                    MODEL_ID,
                    contents,
                    config
            );

            // Handle function calls if present
            response = handleFunctionCalls(response, config, contents);

            return response;
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Gemini Error: " + e.getMessage());
        }
    }

    /**
     * Handle function calls from the model response
     * This method processes any function calls and sends the results back to the model
     */
    private GenerateContentResponse handleFunctionCalls(GenerateContentResponse response, 
                                                         GenerateContentConfig config,
                                                         List<Content> contents) {
        // Check if there are function calls in the response
        List<FunctionCall> functionCalls = response.functionCalls();
        
        if (functionCalls == null || functionCalls.isEmpty()) {
            return response;
        }

        // Add the model's response to contents
        contents.add(response.candidates().get().get(0).content().get());

        // Process each function call and create function responses
        List<FunctionResponse> functionResponses = new ArrayList<>();
        for (FunctionCall functionCall : functionCalls) {
            FunctionResponse functionResponse = functionCallingService.handleFunctionCall(functionCall);
            functionResponses.add(functionResponse);
        }

        // Create content parts with the function responses
        List<Part> responseParts = new ArrayList<>();
        for (FunctionResponse functionResponse : functionResponses) {
            Part part = Part.builder()
                    .functionResponse(functionResponse)
                    .build();
            responseParts.add(part);
        }
        contents.add(Content.fromParts(responseParts.toArray(new Part[0])));

        // Send the function responses back to the model for final response
        return client.models.generateContent(MODEL_ID, contents, config);
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
}