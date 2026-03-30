package com.sep490.vtuber_fanhub.controllers;

import com.sep490.vtuber_fanhub.dto.requests.CreatePostCommentRequest;
import com.sep490.vtuber_fanhub.dto.requests.CreatePostRequest;
import com.sep490.vtuber_fanhub.dto.requests.CreatePollPostRequest;
import com.sep490.vtuber_fanhub.dto.requests.CreateReportPostRequest;
import com.sep490.vtuber_fanhub.dto.responses.APIResponse;
import com.sep490.vtuber_fanhub.dto.responses.PostCommentResponse;
import com.sep490.vtuber_fanhub.dto.responses.PostResponse;
import com.sep490.vtuber_fanhub.dto.responses.SummarizePostResponse;
import com.sep490.vtuber_fanhub.dto.responses.TranslatePostResponse;
import com.sep490.vtuber_fanhub.dto.responses.ReportPostResponse;
import com.sep490.vtuber_fanhub.services.PostCommentService;
import com.sep490.vtuber_fanhub.services.PostService;
import com.sep490.vtuber_fanhub.services.ReportPostService;
import com.sep490.vtuber_fanhub.services.UserBookmarkService;
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

    private final UserBookmarkService userBookmarkService;

    private final PostCommentService postCommentService;

    private final ReportPostService reportPostService;

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

    @PostMapping("/poll")
    @PreAuthorize("hasAnyRole('USER', 'VTUBER')")
    public ResponseEntity<?> createPollPost(@RequestBody @Valid CreatePollPostRequest request) {
        return ResponseEntity.ok().body(APIResponse.<String>builder()
                .success(true)
                .message("Poll post created successfully")
                .data(postService.createPollPost(request))
                .build()
        );
    }

    @GetMapping("/fan-hub/{fanHubId}/pending")
    @PreAuthorize("hasAnyRole('VTUBER', 'USER')")
    public ResponseEntity<?> getPendingPosts(@PathVariable long fanHubId,
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

    @PostMapping("/validate")
    @PreAuthorize("hasAnyRole('VTUBER', 'USER')")
    public ResponseEntity<?> sendAiValidate(@RequestParam Long postId) {

        return ResponseEntity.ok().body(APIResponse.<String>builder()
                .success(true)
                .message("Success")
                .data(postService.sendAiValidate(postId))
                .build()
        );
    }

    @GetMapping("/translate")
    public ResponseEntity<?> translatePost(@RequestParam Long postId) {

        return ResponseEntity.ok().body(APIResponse.<TranslatePostResponse>builder()
                .success(true)
                .message("Success")
                .data(postService.translatePost(postId))
                .build()
        );
    }

    @GetMapping("/summarize")
    public ResponseEntity<?> summarizePost(@RequestParam Long postId) {

        return ResponseEntity.ok().body(APIResponse.<SummarizePostResponse>builder()
                .success(true)
                .message("Success")
                .data(postService.summarizePost(postId))
                .build()
        );
    }

    @GetMapping("/fan-hub/{fanHubId}")
    public ResponseEntity<?> getPosts(@PathVariable long fanHubId,
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

    @GetMapping("/fan-hub/{fanHubId}/announcements-events")
    public ResponseEntity<?> getAnnouncementAndEventPosts(@PathVariable long fanHubId,
                                                          @RequestParam(defaultValue = "0") int pageNo,
                                                          @RequestParam(defaultValue = "10") int pageSize,
                                                          @RequestParam(defaultValue = "createdAt") String sortBy) {

        return ResponseEntity.ok().body(APIResponse.<List<PostResponse>>builder()
                .success(true)
                .message("Success")
                .data(postService.getAnnouncementAndEventPosts(fanHubId, pageNo, pageSize, sortBy))
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

    @PostMapping("/like")
    @PreAuthorize("hasAnyRole('USER', 'VTUBER')")
    public ResponseEntity<?> likePost(@RequestParam long postId) {
        return ResponseEntity.ok().body(APIResponse.<String>builder()
                .success(true)
                .message("Success")
                .data(postService.likePost(postId))
                .build()
        );
    }

    @PostMapping("/unlike")
    @PreAuthorize("hasAnyRole('USER', 'VTUBER')")
    public ResponseEntity<?> unlikePost(@RequestParam long postId) {
        return ResponseEntity.ok().body(APIResponse.<String>builder()
                .success(true)
                .message("Success")
                .data(postService.unlikePost(postId))
                .build()
        );
    }

    @PutMapping("/{postId}/pin")
    @PreAuthorize("hasAnyRole('VTUBER', 'MODERATOR')")
    public ResponseEntity<?> pinPost(@PathVariable long postId) {
        return ResponseEntity.ok().body(APIResponse.<String>builder()
                .success(true)
                .message("Success")
                .data(postService.pinPost(postId))
                .build()
        );
    }

    @PutMapping("/{postId}/reject")
    @PreAuthorize("hasAnyRole('VTUBER', 'MODERATOR')")
    public ResponseEntity<?> rejectPost(@PathVariable long postId,
                                        @RequestParam(required = false) String reason) {
        return ResponseEntity.ok().body(APIResponse.<String>builder()
                .success(true)
                .message("Success")
                .data(postService.rejectPost(postId, reason))
                .build()
        );
    }

    @PostMapping("/bookmark")
    @PreAuthorize("hasAnyRole('USER', 'VTUBER')")
    public ResponseEntity<?> bookmarkPost(@RequestParam long postId) {
        return ResponseEntity.ok().body(APIResponse.<String>builder()
                .success(true)
                .message("Success")
                .data(userBookmarkService.createUserBookmark(postId))
                .build()
        );
    }

    @PostMapping("/comment")
    @PreAuthorize("hasAnyRole('USER', 'VTUBER')")
    public ResponseEntity<?> commentPost(@RequestBody @Valid CreatePostCommentRequest createPostCommentRequest) {
        return ResponseEntity.ok().body(APIResponse.<Boolean>builder()
                .success(true)
                .message("Success")
                .data(postCommentService.createPostComment(createPostCommentRequest))
                .build()
        );
    }

    @GetMapping("/{postId}/comments")
    public ResponseEntity<?> getPostComments(@PathVariable long postId) {
        return ResponseEntity.ok().body(APIResponse.<List<PostCommentResponse>>builder()
                .success(true)
                .message("Success")
                .data(postCommentService.getPostCommentsByPostId(postId))
                .build()
        );
    }

    @PostMapping("/comment/like/{commentId}")
    @PreAuthorize("hasAnyRole('USER', 'VTUBER')")
    public ResponseEntity<?> likeComment(@PathVariable long commentId) {
        return ResponseEntity.ok().body(APIResponse.<String>builder()
                .success(true)
                .message("Success")
                .data(postCommentService.likeComment(commentId))
                .build()
        );
    }

    @PostMapping("/comment/unlike/{commentId}")
    @PreAuthorize("hasAnyRole('USER', 'VTUBER')")
    public ResponseEntity<?> unlikeComment(@PathVariable long commentId) {
        return ResponseEntity.ok().body(APIResponse.<String>builder()
                .success(true)
                .message("Success")
                .data(postCommentService.unlikeComment(commentId))
                .build()
        );
    }

    @PostMapping("/comment/gift/{commentId}")
    @PreAuthorize("hasAnyRole('USER', 'VTUBER')")
    public ResponseEntity<?> sendCommentGift(@PathVariable Long commentId) {
        return ResponseEntity.ok().body(APIResponse.<String>builder()
                .success(true)
                .message("Success")
                .data(postCommentService.sendCommentGift(commentId))
                .build()
        );
    }

    @PostMapping("/vote")
    @PreAuthorize("hasAnyRole('USER', 'VTUBER')")
    public ResponseEntity<?> votePost(@RequestParam long postId, @RequestParam long optionId) {
        return ResponseEntity.ok().body(APIResponse.<String>builder()
                .success(true)
                .message("Success")
                .data(postService.votePost(postId, optionId))
                .build()
        );
    }

    @PostMapping("/un-vote")
    @PreAuthorize("hasAnyRole('USER', 'VTUBER')")
    public ResponseEntity<?> unVotePost(@RequestParam long postId) {
        return ResponseEntity.ok().body(APIResponse.<String>builder()
                .success(true)
                .message("Success")
                .data(postService.unVotePost(postId))
                .build()
        );
    }

    @PostMapping("/report")
    @PreAuthorize("hasAnyRole('USER', 'VTUBER')")
    public ResponseEntity<?> reportPost(@RequestBody CreateReportPostRequest createReportPostRequest) {
        return ResponseEntity.ok().body(APIResponse.<String>builder()
                .success(true)
                .message("Success")
                .data(reportPostService.createReportPost(createReportPostRequest))
                .build()
        );
    }

    @GetMapping("/reports/posts/{fanHubId}")
    @PreAuthorize("hasAnyRole('USER', 'VTUBER')")
    public ResponseEntity<?> getReportPostsByFanHubId(
            @PathVariable Long fanHubId,
            @RequestParam(defaultValue = "0") int pageNo,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(defaultValue = "createdAt") String sortBy) {

        return ResponseEntity.ok().body(APIResponse.<List<ReportPostResponse>>builder()
                .success(true)
                .message("Success")
                .data(reportPostService.getReportPostsByFanHubId(fanHubId, pageNo, pageSize, sortBy))
                .build()
        );
    }

    @GetMapping("/fan-hub/subdomain/{subdomain}")
    public ResponseEntity<?> getPostsBySubDomain(@PathVariable String subdomain,
                                      @RequestParam(defaultValue = "0") int pageNo,
                                      @RequestParam(defaultValue = "10") int pageSize,
                                      @RequestParam(defaultValue = "createdAt") String sortBy,
                                      @RequestParam(required = false) String postHashtag) {

        return ResponseEntity.ok().body(APIResponse.<List<PostResponse>>builder()
                .success(true)
                .message("Success")
                .data(postService.getPostsBySubdomain(subdomain, pageNo, pageSize, sortBy, postHashtag))
                .build()
        );
    }

    @GetMapping("/fan-hub/subdomain/{subdomain}/pending")
    @PreAuthorize("hasAnyRole('VTUBER', 'USER')")
    public ResponseEntity<?> getPendingPostsBySubdomain(@PathVariable String subdomain,
                                             @RequestParam(defaultValue = "0") int pageNo,
                                             @RequestParam(defaultValue = "10") int pageSize,
                                             @RequestParam(defaultValue = "createdAt") String sortBy) {

        return ResponseEntity.ok().body(APIResponse.<List<PostResponse>>builder()
                .success(true)
                .message("Success")
                .data(postService.getPendingPostsBySubdomain(subdomain, pageNo, pageSize, sortBy))
                .build()
        );
    }

    @GetMapping("/user/{username}")
    public ResponseEntity<?> getPostsByUsername(@PathVariable String username,
                                                @RequestParam(defaultValue = "0") int pageNo,
                                                @RequestParam(defaultValue = "10") int pageSize,
                                                @RequestParam(defaultValue = "createdAt") String sortBy) {

        return ResponseEntity.ok().body(APIResponse.<List<PostResponse>>builder()
                .success(true)
                .message("Success")
                .data(postService.getPostsByUsername(username, pageNo, pageSize, sortBy))
                .build()
        );
    }
}
