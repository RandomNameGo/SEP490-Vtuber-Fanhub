package com.sep490.vtuber_fanhub.controllers;

import com.sep490.vtuber_fanhub.dto.requests.FanHubJoinQuestionRequest;
import com.sep490.vtuber_fanhub.dto.responses.APIResponse;
import com.sep490.vtuber_fanhub.dto.responses.FanHubJoinQuestionResponse;
import com.sep490.vtuber_fanhub.services.FanHubJoinQuestionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("vhub/api/v1/fan-hub-join-questions")
@RequiredArgsConstructor
public class FanHubJoinQuestionController {

    private final FanHubJoinQuestionService questionService;

    @GetMapping("/hub/{hubId}")
    public ResponseEntity<?> getQuestionsByHubId(@PathVariable Long hubId) {
        return ResponseEntity.ok().body(APIResponse.<List<FanHubJoinQuestionResponse>>builder()
                .success(true)
                .message("Success")
                .data(questionService.getQuestionsByHubId(hubId))
                .build()
        );
    }

    @PostMapping("/hub/{hubId}")
    @PreAuthorize("hasRole('VTUBER')")
    public ResponseEntity<?> createQuestion(@PathVariable Long hubId, @RequestBody FanHubJoinQuestionRequest request) {
        return ResponseEntity.ok().body(APIResponse.<String>builder()
                .success(true)
                .message("Success")
                .data(questionService.createQuestion(hubId, request))
                .build()
        );
    }

    @PutMapping("/{questionId}")
    @PreAuthorize("hasRole('VTUBER')")
    public ResponseEntity<?> updateQuestion(@PathVariable Long questionId, @RequestBody FanHubJoinQuestionRequest request) {
        return ResponseEntity.ok().body(APIResponse.<String>builder()
                .success(true)
                .message("Success")
                .data(questionService.updateQuestion(questionId, request))
                .build()
        );
    }

    @DeleteMapping("/{questionId}")
    @PreAuthorize("hasRole('VTUBER')")
    public ResponseEntity<?> deleteQuestion(@PathVariable Long questionId) {
        return ResponseEntity.ok().body(APIResponse.<String>builder()
                .success(true)
                .message("Success")
                .data(questionService.deleteQuestion(questionId))
                .build()
        );
    }
}
