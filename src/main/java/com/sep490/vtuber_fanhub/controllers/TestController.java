package com.sep490.vtuber_fanhub.controllers;

import com.sep490.vtuber_fanhub.dto.responses.APIResponse;
import com.sep490.vtuber_fanhub.models.Enum.PostMediaType;
import com.sep490.vtuber_fanhub.services.ContentValidationService;
import com.sep490.vtuber_fanhub.services.GeminiAIService;
import com.sep490.vtuber_fanhub.services.GroqAIService;
import com.sep490.vtuber_fanhub.services.SightEngineService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("vhub/api/v1/test")
@RequiredArgsConstructor
public class TestController {
    private final GeminiAIService geminiAIService;
    private final GroqAIService groqAIService;
    private final SightEngineService sightEngineService;
    private final ContentValidationService contentValidationService;

    @GetMapping("/gemini")
    public ResponseEntity<APIResponse<String>> testGeminiPrompt(){
        return ResponseEntity.ok(
                APIResponse.<String>builder()
                        .message("Test Gemini Success")
                        .success(true)
                        .data(geminiAIService.test())
                        .build()
        );
    }

    @PostMapping("/sightEngineImage")
    public ResponseEntity<APIResponse<String>> checkImage(@RequestParam("file") MultipartFile file) {
        return ResponseEntity.ok(
                APIResponse.<String>builder()
                        .message("nice")
                        .success(true)
                        .data(sightEngineService.checkMediaFile(file, PostMediaType.IMAGE).toString())
                        .build()
        );
    }

    @PostMapping("/sightEngineVideo")
    public ResponseEntity<APIResponse<String>> checkVideo(@RequestParam("file") MultipartFile file) {
        return ResponseEntity.ok(
                APIResponse.<String>builder()
                        .message("nice")
                        .success(true)
                        .data(sightEngineService.checkMediaFile(file, PostMediaType.VIDEO).toString())
                        .build()
        );
    }

    @PostMapping("/mediaValidation")
    public ResponseEntity<APIResponse<String>> mediaValidationTest(@RequestParam("file") MultipartFile file) {
        return ResponseEntity.ok(
                APIResponse.<String>builder()
                        .message("nice")
                        .success(true)
                        .data(contentValidationService.validateImageFile(file))
                        .build()
        );
    }

    @PostMapping("/mediaUrlValidation")
    public ResponseEntity<APIResponse<String>> mediaValidationUrlTest(@RequestParam("url") String url) {
        return ResponseEntity.ok(
                APIResponse.<String>builder()
                        .message("nice")
                        .success(true)
                        .data(contentValidationService.validateImageUrl(url))
                        .build()
        );
    }

    @PostMapping("/textValidation")
    public ResponseEntity<APIResponse<String>> textValidationTest(@RequestBody String text) {
        return ResponseEntity.ok(
                APIResponse.<String>builder()
                        .message("nice")
                        .success(true)
                        .data(contentValidationService.validateText(text))
                        .build()
        );
    }

    @GetMapping("/groq")
    public ResponseEntity<APIResponse<String>> testGroqPrompt() {
        return ResponseEntity.ok(
                APIResponse.<String>builder()
                        .message("Test Groq Success")
                        .success(true)
                        .data(groqAIService.test())
                        .build()
        );
    }
}
