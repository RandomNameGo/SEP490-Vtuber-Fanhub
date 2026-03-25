package com.sep490.vtuber_fanhub.controllers;

import com.sep490.vtuber_fanhub.dto.requests.CreateFanHubRequest;
import com.sep490.vtuber_fanhub.dto.responses.APIResponse;
import com.sep490.vtuber_fanhub.dto.responses.FanHubResponse;
import com.sep490.vtuber_fanhub.services.FanHubService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("vhub/api/v1/fan-hub")
@RequiredArgsConstructor
public class FanHubController {

    private final FanHubService fanHubService;

    @PostMapping("/create")
    @PreAuthorize("hasRole('VTUBER')")
    public ResponseEntity<?> createSystemAccount(@RequestBody @Valid CreateFanHubRequest request) {
        return ResponseEntity.ok().body(APIResponse.<String>builder()
                .success(true)
                .message("Success")
                .data(fanHubService.createFanHub(request))
                .build()
        );
    }

    @PostMapping("/upload-images/{fanHubId}")
    @PreAuthorize("hasRole('VTUBER')")
    public ResponseEntity<?> uploadFanHubBannerBackgroundAvatar(@PathVariable Long fanHubId,
                                                                @RequestParam(value = "banner", required = false) MultipartFile banner,
                                                                @RequestParam(value = "background", required = false) MultipartFile background,
                                                                @RequestParam(value = "avatar", required = false) MultipartFile avatar) throws Exception {
        return ResponseEntity.ok().body(APIResponse.<String>builder()
                .success(true)
                .message("Success")
                .data(fanHubService.uploadFanHubBannerBackGroundAvatar(fanHubId, banner, background, avatar))
                .build()
        );
    }

    @GetMapping("/all")
    public ResponseEntity<?> getAllFanHubs(
            @RequestParam(defaultValue = "0") int pageNo,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "false") boolean includePrivate) {

        return ResponseEntity.ok().body(APIResponse.<List<FanHubResponse>>builder()
                .success(true)
                .message("Success")
                .data(fanHubService.getAllFanHubs(pageNo, pageSize, sortBy, includePrivate))
                .build()
        );
    }

    @GetMapping("/top")
    public ResponseEntity<?> getTopFanHubs(
            @RequestParam(defaultValue = "0") int pageNo,
            @RequestParam(defaultValue = "10") int pageSize) {

        return ResponseEntity.ok().body(APIResponse.<List<FanHubResponse>>builder()
                .success(true)
                .message("Success")
                .data(fanHubService.getTopFanHubs(pageNo, pageSize))
                .build()
        );
    }
}
