package com.sep490.vtuber_fanhub.services;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.genai.types.FunctionCall;
import com.google.genai.types.FunctionResponse;
import com.sep490.vtuber_fanhub.dto.responses.PostResponse;
import com.sep490.vtuber_fanhub.exceptions.CustomAuthenticationException;
import com.sep490.vtuber_fanhub.models.Post;
import com.sep490.vtuber_fanhub.models.User;
import com.sep490.vtuber_fanhub.repositories.TestingPostRepository;
import com.sep490.vtuber_fanhub.repositories.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.TreeMap;

@Service
@RequiredArgsConstructor
public class FunctionCallingService {
    private final UserRepository userRepository;

    private final TestingPostRepository testingPostRepository;

    public String get_display_name(Long userId) {
        try{
            System.out.println("get_display_name used");
            User user = userRepository.findById(userId)
                    .orElseThrow(()-> new RuntimeException("User not found."));
            return user.getDisplayName();
        }
        catch (Exception e) {
            System.out.println("get_display_name failed");
            System.out.println(e.getMessage());
            return "get_display_name failed, inform user about this...";
        }
    }

    public String get_random_post(){
        try{
            System.out.println("Get random post used");
            Post post = testingPostRepository.findTop1ByOrderByIdDesc();
            
            Map<String, Object> postMap = new TreeMap<>();
            postMap.put("postId", post.getId());
            postMap.put("fanHubId", post.getHub().getId());
            postMap.put("authorId", post.getUser().getId());
            postMap.put("authorUsername", post.getUser().getUsername());
            postMap.put("authorDisplayName", post.getUser().getDisplayName());
            postMap.put("postType", post.getPostType());
            postMap.put("title", post.getTitle());
            postMap.put("content", post.getContent());
            postMap.put("status", post.getStatus());
            postMap.put("isPinned", post.getIsPinned());
            
            ObjectMapper mapper = new ObjectMapper();
            String postJson = mapper.writeValueAsString(postMap);
            
            System.out.println("Returning post with ID: " + post.getId());
            return postJson;
        }
        catch (Exception e) {
            System.out.println("Failed to return post");
            System.out.println(e.getMessage());
            return "failed getting post";
        }
    }



    public String test_function_call() {
        System.out.println("test_funcion_call used");
        return "if you are reading this, response with 'AAAAAAAAAAAAAAAAAAAHHHHHHHHHHHHHHH!' ";
    }

    /**
     * Handle function calls from Gemini API
     * This method processes FunctionCall objects and returns FunctionResponse objects
     * 
     * @param functionCall The function call from Gemini
     * @return FunctionResponse with the result
     */
    public FunctionResponse handleFunctionCall(FunctionCall functionCall, Long userId) {
        String functionName = functionCall.name().orElse("");
        Map<String, Object> args = (Map<String, Object>) functionCall.args().orElse(new HashMap<>());

        try {
            String result;

            switch (functionName) {
                case "test_function_call" -> {
                    result = test_function_call();
                }
                case "get_display_name" -> {
                    result = get_display_name(userId);
                }
                case "get_random_post" ->{
                    result = get_random_post();
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
