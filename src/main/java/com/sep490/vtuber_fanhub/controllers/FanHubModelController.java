package com.sep490.vtuber_fanhub.controllers;

import com.sep490.vtuber_fanhub.dto.responses.APIResponse;
import com.sep490.vtuber_fanhub.dto.responses.FanHubModelResponse;
import com.sep490.vtuber_fanhub.services.FanHubModelService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RestController
@RequestMapping("vhub/api/v1/fan-hub-model")
@RequiredArgsConstructor
public class FanHubModelController {

    private final FanHubModelService fanHubModelService;

    @PostMapping("/upload/{fanHubId}")
    @PreAuthorize("hasRole('VTUBER')")
    public ResponseEntity<APIResponse<FanHubModelResponse>> uploadModel(
            @PathVariable Long fanHubId,
            @RequestParam("name") String name,
            @RequestParam(value = "modelFile", required = false) MultipartFile modelFile,
            @RequestParam(value = "spriteFile", required = false) MultipartFile spriteFile) throws IOException {

        return ResponseEntity.ok(APIResponse.<FanHubModelResponse>builder()
                .success(true)
                .message("Model uploaded successfully")
                .data(fanHubModelService.uploadModel(fanHubId, name, modelFile, spriteFile))
                .build());
    }

    @GetMapping("/{fanHubId}")
    public ResponseEntity<APIResponse<FanHubModelResponse>> getModel(@PathVariable Long fanHubId) {
        return ResponseEntity.ok(APIResponse.<FanHubModelResponse>builder()
                .success(true)
                .message("Success")
                .data(fanHubModelService.getModelByFanHubId(fanHubId))
                .build());
    }
}
