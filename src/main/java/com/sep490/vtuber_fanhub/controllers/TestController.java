package com.sep490.vtuber_fanhub.controllers;

import com.sep490.vtuber_fanhub.dto.responses.APIResponse;
import com.sep490.vtuber_fanhub.services.GeminiAIService;
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
    private final SightEngineService sightEngineService;

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

    @PostMapping("/sightEngine")
    public ResponseEntity<APIResponse<String>> checkImage(@RequestParam("file") MultipartFile file) {
        return ResponseEntity.ok(
                APIResponse.<String>builder()
                        .message("nice")
                        .success(true)
                        .data(sightEngineService.checkImage(file))
                        .build()
        );
    }
}
