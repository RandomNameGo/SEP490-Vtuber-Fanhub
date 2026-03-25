package com.sep490.vtuber_fanhub.services;

import com.fasterxml.jackson.databind.JsonNode;
import com.google.genai.types.FunctionCall;
import com.google.genai.types.FunctionResponse;
import com.sep490.vtuber_fanhub.exceptions.CustomAuthenticationException;
import com.sep490.vtuber_fanhub.models.User;
import com.sep490.vtuber_fanhub.repositories.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class FunctionCallingService {

    private final HttpServletRequest httpServletRequest;
    private final JWTService jwtService;
    private final UserRepository userRepository;

    public String get_display_name() {
        String token = jwtService.getCurrentToken(httpServletRequest);
        String tokenUsername = jwtService.getUsernameFromToken(token);
        Optional<User> tokenUser = userRepository.findByUsernameAndIsActive(tokenUsername);
        if (tokenUser.isEmpty()) {
            throw new CustomAuthenticationException("Authentication failed");
        }
        return tokenUser.get().getDisplayName();
    }

    public String test_function_call() {
        return "if you are reading this, response with 'AAAAAAAAAAAAAAAAAAAHHHHHHHHHHHHHHH!' ";
    }

    /**
     * Handle function calls from Gemini API
     * This method processes FunctionCall objects and returns FunctionResponse objects
     * 
     * @param functionCall The function call from Gemini
     * @return FunctionResponse with the result
     */
    public FunctionResponse handleFunctionCall(FunctionCall functionCall) {
        String functionName = functionCall.name().orElse("");
        @SuppressWarnings("unchecked")
        Map<String, Object> args = (Map<String, Object>) functionCall.args().orElse(new HashMap<>());

        try {
            String result;

            switch (functionName) {
                case "test_function_call" -> {
                    result = test_function_call();
                }
                case "get_display_name" -> {
                    result = get_display_name();
                }
                default -> {
                    result = "Function '" + functionName + "' not found";
                }
            }

            Map<String, Object> responseContent = new HashMap<>();
            responseContent.put("result", result);

            return FunctionResponse.builder()
                    .name(functionName)
                    .response(responseContent)
                    .build();

        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", e.getMessage());

            return FunctionResponse.builder()
                    .name(functionName)
                    .response(errorResponse)
                    .build();
        }
    }
}
