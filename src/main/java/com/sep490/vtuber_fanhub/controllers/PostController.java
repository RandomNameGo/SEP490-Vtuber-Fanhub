package com.sep490.vtuber_fanhub.controllers;

import com.sep490.vtuber_fanhub.dto.requests.CreatePostRequest;
import com.sep490.vtuber_fanhub.dto.responses.APIResponse;
import com.sep490.vtuber_fanhub.dto.responses.PostResponse;
import com.sep490.vtuber_fanhub.services.PostService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("vhub/api/v1/posts")
@RequiredArgsConstructor
public class PostController {

    private final PostService postService;

    @PostMapping
    @PreAuthorize("hasAnyRole('USER', 'VTUBER')")
    public ResponseEntity<?> createPost(
            @RequestPart("request") @Valid CreatePostRequest request,
            @RequestPart(value = "images", required = false) List<MultipartFile> images,
            @RequestPart(value = "video", required = false) MultipartFile video) {

        return ResponseEntity.ok().body(APIResponse.<String>builder()
                .success(true)
                .message("Post created successfully")
                .data(postService.createPost(request, images, video))
                .build()
        );
    }

    @GetMapping("/fan-hub/{fanHubId}/pending")
    @PreAuthorize("hasAnyRole('VTUBER', 'MODERATOR')")
    public ResponseEntity<?> getPendingPosts(@PathVariable Long fanHubId,
                                             @RequestParam(defaultValue = "0") int pageNo,
                                             @RequestParam(defaultValue = "10") int pageSize,
                                             @RequestParam(defaultValue = "createdAt") String sortBy) {

        return ResponseEntity.ok().body(APIResponse.<List<PostResponse>>builder()
                .success(true)
                .message("Success")
                .data(postService.getPendingPosts(fanHubId, pageNo, pageSize, sortBy))
                .build()
        );
    }

    @GetMapping("/fan-hub/{fanHubId}")
    public ResponseEntity<?> getPosts(@PathVariable Long fanHubId,
                                      @RequestParam(defaultValue = "0") int pageNo,
                                      @RequestParam(defaultValue = "10") int pageSize,
                                      @RequestParam(defaultValue = "createdAt") String sortBy,
                                      @RequestParam(required = false) String postHashtag) {

        return ResponseEntity.ok().body(APIResponse.<List<PostResponse>>builder()
                .success(true)
                .message("Success")
                .data(postService.getPosts(fanHubId, pageNo, pageSize, sortBy, postHashtag))
                .build()
        );
    }

    @PutMapping("/review")
    @PreAuthorize("hasAnyRole('VTUBER', 'USER')")
    public ResponseEntity<?> reviewPost(
            @RequestParam Long postId,
            @RequestParam String status) {

        return ResponseEntity.ok().body(APIResponse.<String>builder()
                .success(true)
                .message(postService.reviewPost(postId, status))
                .build()
        );
    }

    @GetMapping("/feed")
    public ResponseEntity<?> getPersonalizedFeed(
            @RequestParam(defaultValue = "0") int pageNo,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(defaultValue = "createdAt") String sortBy) {

        return ResponseEntity.ok().body(APIResponse.<List<PostResponse>>builder()
                .success(true)
                .message("Success")
                .data(postService.getPersonalizedFeed(pageNo, pageSize, sortBy))
                .build()
        );
    }
}
