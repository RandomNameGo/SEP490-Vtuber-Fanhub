package com.sep490.vtuber_fanhub.services;

import com.sep490.vtuber_fanhub.dto.requests.CreatePollPostRequest;
import com.sep490.vtuber_fanhub.dto.requests.CreatePostRequest;
import com.sep490.vtuber_fanhub.dto.responses.SummarizePostResponse;
import com.sep490.vtuber_fanhub.dto.responses.PostResponse;
import com.sep490.vtuber_fanhub.dto.responses.TranslatePostResponse;
import com.sep490.vtuber_fanhub.dto.responses.PostWithMediaResponse;
import com.sep490.vtuber_fanhub.dto.responses.VoteOptionResponse;
import com.sep490.vtuber_fanhub.events.PostCreatedEvent;
import com.sep490.vtuber_fanhub.exceptions.CooldownException;
import com.sep490.vtuber_fanhub.exceptions.CustomAuthenticationException;
import com.sep490.vtuber_fanhub.exceptions.NotFoundException;
import com.sep490.vtuber_fanhub.models.*;
import com.sep490.vtuber_fanhub.repositories.*;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.context.ApplicationEventPublisher;

import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Service implementation for Post management
 * Handles post creation, validation, likes, comments
 * Sends SSE notifications for post interactions (likes, comments)
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class PostServiceImpl implements PostService {

    private final PostRepository postRepository;

    private final PostHashtagRepository postHashtagRepository;

    private final PostMediaRepository postMediaRepository;

    private final PostLikeRepository postLikeRepository;

    private final FanHubRepository fanHubRepository;

    private final FanHubMemberRepository fanHubMemberRepository;

    private final UserRepository userRepository;

    private final HttpServletRequest httpServletRequest;

    private final CloudinaryService cloudinaryService;

    private final FanHubCategoryRepository fanHubCategoryRepository;

    private final ApplicationEventPublisher eventPublisher;

    private final UserDailyMissionRepository userDailyMissionRepository;

    private final VoteOptionRepository voteOptionRepository;

    private final AuthService authService;

    // SSE notification service for sending real-time updates to users
    private final BanMemberService banMemberService;
    // Note: Using NotificationService which handles both DB persistence and SSE delivery
    private final NotificationService notificationService;

    private final PostVoteRepository postVoteRepository;

    private final PostCommentRepository postCommentRepository;

    private final PostValidationService postValidationServiceImplAsync;

    private final JWTService jwtService;

    private static final double FOLLOWED_RATIO = 0.7;
    private static final double SUGGESTION_RATIO = 0.3;
    private final long AI_VALIDATION_COOLDOWN_MINUTES = 20;
    private final GeminiAIServiceImpl geminiAIServiceImpl;

    private final UserTrackService userTrackService;

    private final UserDailyMissionService userDailyMissionService;

    private final UserBookmarkRepository userBookmarkRepository;

    private final org.springframework.data.redis.core.StringRedisTemplate redisTemplate;

    private final ItemRepository itemRepository;

    private static final String TRENDING_POST_INDEX_KEY = "trending_post_rotation_index";
    private static final String LATEST_POST_INDEX_KEY = "latest_post_rotation_index";

    @Override
    @Transactional
    public String createPost(CreatePostRequest request, List<MultipartFile> images, MultipartFile video) {
        User currentUser = authService.getUserFromToken(httpServletRequest);

        Optional<FanHub> fanHub = fanHubRepository.findById(request.getFanHubId());
        if (fanHub.isEmpty()) {
            throw new NotFoundException("FanHub not found");
        }

        // Check if user is banned from creating posts in this hub
        banMemberService.checkBanStatus(request.getFanHubId(), currentUser.getId(), List.of("POST"));

        boolean isOwner = fanHub.get().getOwnerUser().getId().equals(currentUser.getId());

        Optional<FanHubMember> member = fanHubMemberRepository.findByHubIdAndUserId(
                request.getFanHubId(), currentUser.getId());

        if (!isOwner && member.isEmpty()) {
            throw new AccessDeniedException("You must be the owner (VTuber) or a member of this FanHub to create a post");
        }

        // Validate post type
        String postType = request.getPostType().toUpperCase();
        if (!List.of("TEXT", "IMAGE", "VIDEO", "POLL").contains(postType)) {
            throw new IllegalArgumentException("Invalid post type. Must be TEXT, IMAGE, VIDEO, or POLL");
        }

        // Only VTUBER owner can set isAnnouncement or isSchedule
        if ((request.getIsAnnouncement() != null && request.getIsAnnouncement()) ||
            (request.getIsSchedule() != null && request.getIsSchedule())) {
            if (!"VTUBER".equals(currentUser.getRole()) || !isOwner) {
                throw new AccessDeniedException("Only the VTUBER (owner) of this FanHub can create announcement or schedule posts");
            }
        }

        // Validate media based on post type
        if ("IMAGE".equals(postType)) {
            if (images == null || images.isEmpty()) {
                throw new IllegalArgumentException("IMAGE post type requires at least one image");
            }
            if (images.size() > 4) {
                throw new IllegalArgumentException("Maximum 4 images allowed per post");
            }
            for (MultipartFile image : images) {
                if (image.isEmpty()) {
                    throw new IllegalArgumentException("One or more image files are empty");
                }
            }
        }

        if ("VIDEO".equals(postType)) {
            if (video == null || video.isEmpty()) {
                throw new IllegalArgumentException("VIDEO post type requires a video file");
            }
        }

        boolean isAnnouncement = request.getIsAnnouncement() != null && request.getIsAnnouncement();
        boolean isSchedule = request.getIsSchedule() != null && request.getIsSchedule();

        // Create the post
        Post post = new Post();
        post.setHub(fanHub.get());
        post.setUser(currentUser);
        post.setPostType(postType);
        post.setTitle(request.getTitle());
        post.setContent(request.getContent());
        post.setIsPinned(false);
        post.setStatus(isAnnouncement || isSchedule ? "APPROVED" : "PENDING");
        post.setIsAnnouncement(isAnnouncement);
        post.setIsSchedule(isSchedule);
        post.setStartTime(request.getStartTime());
        post.setEndTime(request.getEndTime());
        post.setCreatedAt(Instant.now());
        post.setUpdatedAt(Instant.now());

        post = postRepository.save(post);

        try {
            if ("IMAGE".equals(postType)) {
                for (MultipartFile image : images) {
                    String imageUrl = cloudinaryService.uploadFile(image);
                    PostMedia postMedia = new PostMedia();
                    postMedia.setPost(post);
                    postMedia.setMediaUrl(imageUrl);
                    postMedia.setAiValidationStatus("PENDING");
                    postMediaRepository.save(postMedia);
                }
            }

            if ("VIDEO".equals(postType)) {
                String videoUrl = cloudinaryService.uploadVideo(video);
                PostMedia postMedia = new PostMedia();
                postMedia.setPost(post);
                postMedia.setMediaUrl(videoUrl);
                postMedia.setAiValidationStatus("PENDING");
                postMediaRepository.save(postMedia);
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to upload media: " + e.getMessage(), e);
        }

        // Save hashtags if provided
        if (request.getHashtags() != null && !request.getHashtags().isEmpty()) {
            for (String hashtag : request.getHashtags()) {
                PostHashtag postHashtag = new PostHashtag();
                postHashtag.setPost(post);
                postHashtag.setHashtag(hashtag);
                postHashtagRepository.save(postHashtag);
            }
        }

        // Publish event to trigger validation after transaction commits
        eventPublisher.publishEvent(new PostCreatedEvent(post));

        return "Created post successfully";
    }

    @Override
    @Transactional
    public String createPollPost(CreatePollPostRequest request) {
        User currentUser = authService.getUserFromToken(httpServletRequest);

        Optional<FanHub> fanHub = fanHubRepository.findById(request.getFanHubId());
        if (fanHub.isEmpty()) {
            throw new NotFoundException("FanHub not found");
        }

        boolean isOwner = fanHub.get().getOwnerUser().getId().equals(currentUser.getId());

        Optional<FanHubMember> member = fanHubMemberRepository.findByHubIdAndUserId(
                request.getFanHubId(), currentUser.getId());
        
        if (!isOwner && member.isEmpty()) {
            throw new AccessDeniedException("You must be the owner (VTuber) or a member of this FanHub to create a post");
        }

        List<String> options = request.getOptions();
        if (options == null || options.size() < 2) {
            throw new IllegalArgumentException("Poll must have at least 2 options");
        }
        if (options.size() > 4) {
            throw new IllegalArgumentException("Poll cannot have more than 4 options");
        }

        // Check for duplicate options
        Set<String> uniqueOptions = new HashSet<>(options);
        if (uniqueOptions.size() != options.size()) {
            throw new IllegalArgumentException("Poll options must be unique");
        }

        // Create the poll post
        Post post = new Post();
        post.setHub(fanHub.get());
        post.setUser(currentUser);
        post.setPostType("POLL");
        post.setTitle(request.getTitle());
        post.setContent(request.getContent());
        post.setIsPinned(false);
        post.setStatus("PENDING");
        post.setFinalAiValidationStatus("PENDING");
        post.setCreatedAt(Instant.now());
        post.setUpdatedAt(Instant.now());

        post = postRepository.save(post);

        // Save vote options
        for (String optionText : options) {
            VoteOption voteOption = new VoteOption();
            voteOption.setPost(post);
            voteOption.setOptionText(optionText);
            voteOptionRepository.save(voteOption);
        }

        // Save hashtags if provided
        if (request.getHashtags() != null && !request.getHashtags().isEmpty()) {
            for (String hashtag : request.getHashtags()) {
                PostHashtag postHashtag = new PostHashtag();
                postHashtag.setPost(post);
                postHashtag.setHashtag(hashtag);
                postHashtagRepository.save(postHashtag);
            }
        }

        // Publish event to trigger validation after transaction commits
        eventPublisher.publishEvent(new PostCreatedEvent(post));

        return "Created poll post successfully";
    }

    @Override
    @Transactional(readOnly = true)
    public List<PostWithMediaResponse> getPendingPosts(Long fanHubId, int pageNo, int pageSize, String sortBy, String sortDir) {
        User currentUser = authService.getUserFromToken(httpServletRequest);

        Optional<FanHub> fanHub = fanHubRepository.findById(fanHubId);
        if (fanHub.isEmpty()) {
            throw new NotFoundException("FanHub not found");
        }

        // Check if user is VTUBER and owns this FanHub
        boolean isOwner = "VTUBER".equals(currentUser.getRole()) &&
                fanHub.get().getOwnerUser().getId().equals(currentUser.getId());

        // Check if user is a member with MODERATOR role
        boolean isModerator = fanHubMemberRepository.findByHubIdAndUserId(fanHubId, currentUser.getId())
                .map(member -> "MODERATOR".equals(member.getRoleInHub()))
                .orElse(false);

        if (!isOwner && !isModerator) {
            throw new AccessDeniedException("Only VTUBER (owner) or MODERATOR can view pending posts");
        }

        Pageable paging = PageRequest.of(pageNo, pageSize, Sort.by(getSortDirection(sortDir), sortBy));

        Page<Post> pagedPosts = postRepository.findByHubIdAndStatus(fanHubId, "PENDING", paging);

        if (pagedPosts.isEmpty()) {
            return List.of();
        }

        return pagedPosts.getContent().stream()
                .map(this::mapToPostWithMediaResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<PostWithMediaResponse> getRejectedPosts(Long fanHubId, int pageNo, int pageSize, String sortBy, String sortDir) {
        User currentUser = authService.getUserFromToken(httpServletRequest);

        Optional<FanHub> fanHub = fanHubRepository.findById(fanHubId);
        if (fanHub.isEmpty()) {
            throw new NotFoundException("FanHub not found");
        }

        // Check if user is VTUBER and owns this FanHub
        boolean isOwner = "VTUBER".equals(currentUser.getRole()) &&
                fanHub.get().getOwnerUser().getId().equals(currentUser.getId());

        // Check if user is a member with MODERATOR role
        boolean isModerator = fanHubMemberRepository.findByHubIdAndUserId(fanHubId, currentUser.getId())
                .map(member -> "MODERATOR".equals(member.getRoleInHub()))
                .orElse(false);

        if (!isOwner && !isModerator) {
            throw new AccessDeniedException("Only VTUBER (owner) or MODERATOR can view rejected posts");
        }

        Pageable paging = PageRequest.of(pageNo, pageSize, Sort.by(getSortDirection(sortDir), sortBy));

        Page<Post> pagedPosts = postRepository.findByHubIdAndStatus(fanHubId, "REJECTED", paging);

        if (pagedPosts.isEmpty()) {
            return List.of();
        }

        return pagedPosts.getContent().stream()
                .map(this::mapToPostWithMediaResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<PostResponse> getPosts(Long fanHubId, int pageNo, int pageSize, String sortBy, String sortDir, String postHashtag, String authorUsername) {
        // Get current user from token (may be null if unauthenticated)
        User currentUser = null;
        try {
            currentUser = authService.getUserFromToken(httpServletRequest);
        } catch (Exception e) {
            // Token is invalid or expired, treat as unauthenticated
            currentUser = null;
        }

        Optional<FanHub> fanHub = fanHubRepository.findById(fanHubId);
        if (fanHub.isEmpty()) {
            throw new NotFoundException("FanHub not found");
        }

        // Check access permissions
        boolean isMember = false;
        if (currentUser != null) {
            Optional<FanHubMember> member = fanHubMemberRepository.findByHubIdAndUserId(
                    fanHubId, currentUser.getId());
            isMember = member.isPresent();
        }

        // If not a member and fanHub is private, deny access
        if (!isMember && fanHub.get().getIsPrivate()) {
            throw new AccessDeniedException("You must be a member of this FanHub to view posts");
        }

        // Pinned posts first, then by user's sortBy
        Sort sort = Sort.by(Sort.Direction.DESC, "isPinned").and(Sort.by(getSortDirection(sortDir), sortBy));
        Pageable paging = PageRequest.of(pageNo, pageSize, sort);

        Page<Post> pagedPosts;
        if (postHashtag != null && !postHashtag.isEmpty()) {
            pagedPosts = postRepository.findByHubIdAndStatusAndHashtagAndAuthor(fanHubId, "APPROVED", postHashtag, authorUsername, paging);
        } else if (authorUsername != null && !authorUsername.isEmpty()) {
            pagedPosts = postRepository.findByHubIdAndStatusAndHashtagAndAuthor(fanHubId, "APPROVED", null, authorUsername, paging);
        } else {
            pagedPosts = postRepository.findByHubIdAndStatus(fanHubId, "APPROVED", paging);
        }

        if (pagedPosts.isEmpty()) {
            return List.of();
        }

        return pagedPosts.getContent().stream()
                .map(this::mapToPostResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<PostResponse> getPostsBySubdomain(String subdomain, int pageNo, int pageSize, String sortBy, String sortDir, String postHashtag, String authorUsername) {
        // Get current user from token (may be null if unauthenticated)
        User currentUser = null;
        try {
            currentUser = authService.getUserFromToken(httpServletRequest);
        } catch (Exception e) {
            // Token is invalid or expired, treat as unauthenticated
            currentUser = null;
        }

        Optional<FanHub> fanHub = fanHubRepository.findBySubdomainAndIsActive(subdomain, true);
        if (fanHub.isEmpty()) {
            throw new NotFoundException("FanHub not found");
        }

        // Check access permissions
        boolean isMember = false;
        if (currentUser != null) {
            Optional<FanHubMember> member = fanHubMemberRepository.findByHubIdAndUserId(
                    fanHub.get().getId(), currentUser.getId());
            isMember = member.isPresent();
        }

        // If not a member and fanHub is private, deny access
        if (!isMember && fanHub.get().getIsPrivate()) {
            throw new AccessDeniedException("You must be a member of this FanHub to view posts");
        }

        // Pinned posts first, then by user's sortBy
        Sort sort = Sort.by(Sort.Direction.DESC, "isPinned").and(Sort.by(getSortDirection(sortDir), sortBy));
        Pageable paging = PageRequest.of(pageNo, pageSize, sort);

        Page<Post> pagedPosts;
        if (postHashtag != null && !postHashtag.isEmpty()) {
            pagedPosts = postRepository.findByHubIdAndStatusAndHashtagAndAuthor(fanHub.get().getId(), "APPROVED", postHashtag, authorUsername, paging);
        } else if (authorUsername != null && !authorUsername.isEmpty()) {
            pagedPosts = postRepository.findByHubIdAndStatusAndHashtagAndAuthor(fanHub.get().getId(), "APPROVED", null, authorUsername, paging);
        } else {
            pagedPosts = postRepository.findByHubIdAndStatus(fanHub.get().getId(), "APPROVED", paging);
        }

        if (pagedPosts.isEmpty()) {
            return List.of();
        }

        return pagedPosts.getContent().stream()
                .map(this::mapToPostResponse)
                .collect(Collectors.toList());

    }

    @Override
    public List<PostWithMediaResponse> getPendingPostsBySubdomain(String subdomain, int pageNo, int pageSize, String sortBy, String sortDir) {
        User currentUser = authService.getUserFromToken(httpServletRequest);

        Optional<FanHub> fanHub = fanHubRepository.findBySubdomainAndIsActive(subdomain, true);
        if (fanHub.isEmpty()) {
            throw new NotFoundException("FanHub not found");
        }

        // Check if user is VTUBER and owns this FanHub
        boolean isOwner = "VTUBER".equals(currentUser.getRole()) &&
                fanHub.get().getOwnerUser().getId().equals(currentUser.getId());

        // Check if user is a member with MODERATOR role
        boolean isModerator = fanHubMemberRepository.findByHubIdAndUserId(fanHub.get().getId(), currentUser.getId())
                .map(member -> "MODERATOR".equals(member.getRoleInHub()))
                .orElse(false);

        if (!isOwner && !isModerator) {
            throw new AccessDeniedException("Only VTUBER (owner) or MODERATOR can view pending posts");
        }

        Pageable paging = PageRequest.of(pageNo, pageSize, Sort.by(getSortDirection(sortDir), sortBy));

        Page<Post> pagedPosts = postRepository.findByHubIdAndStatus(fanHub.get().getId(), "PENDING", paging);

        if (pagedPosts.isEmpty()) {
            return List.of();
        }

        return pagedPosts.getContent().stream()
                .map(this::mapToPostWithMediaResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<PostResponse> getAnnouncementAndEventPosts(Long fanHubId, int pageNo, int pageSize, String sortBy, String sortDir) {
        Optional<FanHub> fanHub = fanHubRepository.findById(fanHubId);
        if (fanHub.isEmpty()) {
            throw new NotFoundException("FanHub not found");
        }

        Pageable paging = PageRequest.of(pageNo, pageSize, Sort.by(getSortDirection(sortDir), sortBy));

        Page<Post> pagedPosts = postRepository.findByHubIdAndStatusAndAnnouncementOrSchedule(
                fanHubId, "APPROVED", paging);

        if (pagedPosts.isEmpty()) {
            return List.of();
        }

        return pagedPosts.getContent().stream()
                .map(this::mapToPostResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public String reviewPost(Long postId, String status) {
        User currentUser = authService.getUserFromToken(httpServletRequest);

        Optional<Post> post = postRepository.findById(postId);
        if (post.isEmpty()) {
            throw new NotFoundException("Post not found");
        }

        String normalizedStatus = status.toUpperCase();
        if (!List.of("APPROVED", "REJECTED").contains(normalizedStatus)) {
            throw new IllegalArgumentException("Invalid status. Must be APPROVED or REJECTED");
        }

        Long fanHubId = post.get().getHub().getId();

        boolean isOwner = "VTUBER".equals(currentUser.getRole()) &&
                fanHubRepository.findById(fanHubId)
                        .map(hub -> hub.getOwnerUser().getId().equals(currentUser.getId()))
                        .orElse(false);

        boolean isModerator = fanHubMemberRepository.findByHubIdAndUserId(fanHubId, currentUser.getId())
                .map(member -> "MODERATOR".equals(member.getRoleInHub()))
                .orElse(false);

        if (!isOwner && !isModerator) {
            throw new AccessDeniedException("Only VTUBER (owner) or MODERATOR can review posts");
        }

        post.get().setStatus(normalizedStatus);
        post.get().setReviewedBy(currentUser);
        post.get().setUpdatedAt(Instant.now());
        postRepository.save(post.get());

        // Award points and send notification when post is APPROVED
        if ("APPROVED".equals(normalizedStatus)) {
            User postAuthor = post.get().getUser();
            
            long currentPoints = postAuthor.getPoints() != null ? postAuthor.getPoints() : 0;
            postAuthor.setPoints(currentPoints + 10);
            userRepository.save(postAuthor);

            Optional<FanHubMember> member = fanHubMemberRepository.findByHubIdAndUserId(
                    fanHubId, postAuthor.getId());
            if (member.isPresent()) {
                FanHubMember fanHubMember = member.get();
                int currentScore = fanHubMember.getFanHubScore() != null ? fanHubMember.getFanHubScore() : 0;
                fanHubMember.setFanHubScore(currentScore + 10);
                fanHubMemberRepository.save(fanHubMember);
            }

            notificationService.sendPostApprovalNotification(
                    postAuthor.getId(),
                    postId,
                    post.get().getTitle(),
                    fanHubId,
                    post.get().getHub().getHubName()
            );
        } else if ("REJECTED".equals(normalizedStatus)) {
            notificationService.sendPostRejectionNotification(
                    post.get().getUser().getId(),
                    postId,
                    post.get().getTitle(),
                    fanHubId,
                    post.get().getHub().getHubName(),
                    post.get().getAiValidationComment()
            );
        }

        return "Post " + normalizedStatus.toLowerCase() + " successfully";
    }

    @Override
    @Transactional
    public String reviewPosts(List<Long> postIds, String status) {
        User currentUser = authService.getUserFromToken(httpServletRequest);

        String normalizedStatus = status.toUpperCase();
        if (!List.of("APPROVED", "REJECTED").contains(normalizedStatus)) {
            throw new IllegalArgumentException("Invalid status. Must be APPROVED or REJECTED");
        }

        int approvedCount = 0;
        int rejectedCount = 0;

        for (Long postId : postIds) {
            Optional<Post> post = postRepository.findById(postId);
            if (post.isEmpty()) {
                throw new NotFoundException("Post not found with id: " + postId);
            }

            Long fanHubId = post.get().getHub().getId();

            boolean isOwner = "VTUBER".equals(currentUser.getRole()) &&
                    fanHubRepository.findById(fanHubId)
                            .map(hub -> hub.getOwnerUser().getId().equals(currentUser.getId()))
                            .orElse(false);

            boolean isModerator = fanHubMemberRepository.findByHubIdAndUserId(fanHubId, currentUser.getId())
                    .map(member -> "MODERATOR".equals(member.getRoleInHub()))
                    .orElse(false);

            if (!isOwner && !isModerator) {
                throw new AccessDeniedException("Only VTUBER (owner) or MODERATOR can review posts");
            }

            post.get().setStatus(normalizedStatus);
            post.get().setReviewedBy(currentUser);
            post.get().setUpdatedAt(Instant.now());
            postRepository.save(post.get());

            // Award points and send notification when post is APPROVED
            if ("APPROVED".equals(normalizedStatus)) {
                User postAuthor = post.get().getUser();

                long currentPoints = postAuthor.getPoints() != null ? postAuthor.getPoints() : 0;
                postAuthor.setPoints(currentPoints + 10);
                userRepository.save(postAuthor);

                Optional<FanHubMember> member = fanHubMemberRepository.findByHubIdAndUserId(
                        fanHubId, postAuthor.getId());
                if (member.isPresent()) {
                    FanHubMember fanHubMember = member.get();
                    int currentScore = fanHubMember.getFanHubScore() != null ? fanHubMember.getFanHubScore() : 0;
                    fanHubMember.setFanHubScore(currentScore + 10);
                    fanHubMemberRepository.save(fanHubMember);
                }

                notificationService.sendPostApprovalNotification(
                        postAuthor.getId(),
                        postId,
                        post.get().getTitle(),
                        fanHubId,
                        post.get().getHub().getHubName()
                );
                approvedCount++;
            } else {
                notificationService.sendPostRejectionNotification(
                        post.get().getUser().getId(),
                        postId,
                        post.get().getTitle(),
                        fanHubId,
                        post.get().getHub().getHubName(),
                        post.get().getAiValidationComment()
                );
                rejectedCount++;
            }
        }

        return "Reviewed " + approvedCount + " approved, " + rejectedCount + " rejected";
    }

    @Override
    @Transactional
    public String approveAiSafePosts(Long fanHubId) {
        User currentUser = authService.getUserFromToken(httpServletRequest);

        boolean isOwner = "VTUBER".equals(currentUser.getRole()) &&
                fanHubRepository.findById(fanHubId)
                        .map(hub -> hub.getOwnerUser().getId().equals(currentUser.getId()))
                        .orElse(false);

        boolean isModerator = fanHubMemberRepository.findByHubIdAndUserId(fanHubId, currentUser.getId())
                .map(member -> "MODERATOR".equals(member.getRoleInHub()))
                .orElse(false);

        if (!isOwner && !isModerator) {
            throw new AccessDeniedException("Only VTUBER (owner) or MODERATOR can approve posts");
        }

        List<Post> aiSafePosts = postRepository.findByHubIdAndAiValidationStatusAndPending(fanHubId, "AI_SAFE");

        if (aiSafePosts.isEmpty()) {
            return "No AI_SAFE posts found to approve";
        }

        int approvedCount = 0;
        for (Post post : aiSafePosts) {
            post.setStatus("APPROVED");
            post.setReviewedBy(currentUser);
            post.setUpdatedAt(Instant.now());
            postRepository.save(post);

            // Award points and send notification when post is APPROVED
            User postAuthor = post.getUser();

            long currentPoints = postAuthor.getPoints() != null ? postAuthor.getPoints() : 0;
            postAuthor.setPoints(currentPoints + 10);
            userRepository.save(postAuthor);

            Optional<FanHubMember> member = fanHubMemberRepository.findByHubIdAndUserId(
                    fanHubId, postAuthor.getId());
            if (member.isPresent()) {
                FanHubMember fanHubMember = member.get();
                int currentScore = fanHubMember.getFanHubScore() != null ? fanHubMember.getFanHubScore() : 0;
                fanHubMember.setFanHubScore(currentScore + 10);
                fanHubMemberRepository.save(fanHubMember);
            }

            notificationService.sendPostApprovalNotification(
                    postAuthor.getId(),
                    post.getId(),
                    post.getTitle(),
                    fanHubId,
                    post.getHub().getHubName()
            );
            approvedCount++;
        }

        return "Approved " + approvedCount + " AI_SAFE post(s) successfully";
    }

    @Override
    @Transactional
    public String rejectAiUnsafePosts(Long fanHubId) {
        User currentUser = authService.getUserFromToken(httpServletRequest);

        boolean isOwner = "VTUBER".equals(currentUser.getRole()) &&
                fanHubRepository.findById(fanHubId)
                        .map(hub -> hub.getOwnerUser().getId().equals(currentUser.getId()))
                        .orElse(false);

        boolean isModerator = fanHubMemberRepository.findByHubIdAndUserId(fanHubId, currentUser.getId())
                .map(member -> "MODERATOR".equals(member.getRoleInHub()))
                .orElse(false);

        if (!isOwner && !isModerator) {
            throw new AccessDeniedException("Only VTUBER (owner) or MODERATOR can reject posts");
        }

        List<Post> aiUnsafePosts = postRepository.findByHubIdAndAiValidationStatusAndPending(fanHubId, "AI_UNSAFE");

        if (aiUnsafePosts.isEmpty()) {
            return "No AI_UNSAFE posts found to reject";
        }

        int rejectedCount = 0;
        for (Post post : aiUnsafePosts) {
            post.setStatus("REJECTED");
            post.setReviewedBy(currentUser);
            post.setUpdatedAt(Instant.now());
            postRepository.save(post);

            notificationService.sendPostRejectionNotification(
                    post.getUser().getId(),
                    post.getId(),
                    post.getTitle(),
                    fanHubId,
                    post.getHub().getHubName(),
                    post.getAiValidationComment()
            );
            rejectedCount++;
        }

        return "Rejected " + rejectedCount + " AI_UNSAFE post(s) successfully";
    }

    @Override
    @Transactional
    public String rejectPost(Long postId, String reason) {
        User currentUser = authService.getUserFromToken(httpServletRequest);

        Optional<Post> post = postRepository.findById(postId);
        if (post.isEmpty()) {
            throw new NotFoundException("Post not found");
        }

        Long fanHubId = post.get().getHub().getId();

        boolean isOwner = "VTUBER".equals(currentUser.getRole()) &&
                fanHubRepository.findById(fanHubId)
                        .map(hub -> hub.getOwnerUser().getId().equals(currentUser.getId()))
                        .orElse(false);

        boolean isModerator = fanHubMemberRepository.findByHubIdAndUserId(fanHubId, currentUser.getId())
                .map(member -> "MODERATOR".equals(member.getRoleInHub()))
                .orElse(false);

        if (!isOwner && !isModerator) {
            throw new AccessDeniedException("Only VTUBER (owner) or MODERATOR can reject posts");
        }

        post.get().setStatus("REJECTED");
        post.get().setUpdatedAt(Instant.now());
        if (reason != null && !reason.isEmpty()) {
            post.get().setAiValidationComment(reason);
        }
        postRepository.save(post.get());

        notificationService.sendPostRejectionNotification(
                post.get().getUser().getId(),
                postId,
                post.get().getTitle(),
                fanHubId,
                post.get().getHub().getHubName(),
                reason
        );

        return "Post rejected successfully";
    }


    @Override
    @Transactional
    public String sendAiValidate(Long postId) {
        String token = jwtService.getCurrentToken(httpServletRequest);
        String tokenUsername = jwtService.getUsernameFromToken(token);

        Optional<User> tokenUser = userRepository.findByUsernameAndIsActive(tokenUsername);
        if (tokenUser.isEmpty()) {
            throw new CustomAuthenticationException("Authentication failed");
        }

        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new NotFoundException("Post not found"));

        Long fanHubId = post.getHub().getId();

        // Check if user is VTUBER and owns this FanHub
        boolean isOwner = "VTUBER".equals(tokenUser.get().getRole()) &&
                fanHubRepository.findById(fanHubId)
                        .map(hub -> hub.getOwnerUser().getId().equals(tokenUser.get().getId()))
                        .orElse(false);

        // Check if user is a member with MODERATOR role
        FanHubMember memberShip = fanHubMemberRepository.findByHub_IdAndUser_Id(fanHubId, tokenUser.get().getId())
                .orElseThrow(() -> new NotFoundException("User is not member of this fanhub"));
        boolean isModerator = memberShip.getRoleInHub().equals("MODERATOR");

        if (!isOwner && !isModerator) {
            throw new AccessDeniedException("Only VTUBER (owner) or MODERATOR can send ai validation job " +
                    "to post of the Same Hub.");
        }


        Instant lastExecutedTime = post.getAiValidationLastSentAt();

        if (lastExecutedTime != null) {
            Instant cooldownEndTime = lastExecutedTime.plus(AI_VALIDATION_COOLDOWN_MINUTES, ChronoUnit.MINUTES);
            Instant now = Instant.now();

            if (now.isBefore(cooldownEndTime)) {
                Duration remaining = Duration.between(now, cooldownEndTime);

                long minutes = remaining.toMinutes();
                long seconds = remaining.toSecondsPart();

                throw new CooldownException(String.format(
                        "Please wait %02d:%02d before validating this post again.",
                        minutes, seconds
                ));
            }
        }
        post.setAiValidationLastSentAt(Instant.now());
        post = postRepository.save(post);

        postValidationServiceImplAsync.validatePost(post);

        return "Job sent successfully!";
    }

    @Override
    public List<PostResponse> getPersonalizedFeed(String hashtag, int pageNo, int pageSize, String sortBy, String sortDir) {
        User currentUser = null;
        try {
            currentUser = authService.getUserFromToken(httpServletRequest);
        } catch (Exception e) {
            // Token is invalid or expired, treat as unauthenticated
            currentUser = null;
        }

        Pageable paging = PageRequest.of(pageNo, pageSize, Sort.by(getSortDirection(sortDir), sortBy));

        // Case 1: Unauthenticated user - return public posts sorted by interactions
        if (currentUser == null) {
            return postRepository.findPublicPostsOrderByInteractions(hashtag, paging).getContent().stream()
                    .map(this::mapToPostResponse)
                    .collect(Collectors.toList());
        }

        List<Long> followedHubIds = getFollowedHubIds(currentUser.getId());

        // Case 2: Authenticated but no followed hubs - return any public posts
        if (followedHubIds.isEmpty()) {
            return postRepository.findPublicPosts(hashtag, Collections.emptyList(), paging).getContent().stream()
                    .map(this::mapToPostResponse)
                    .collect(Collectors.toList());
        }

        // Case 3: Authenticated with followed hubs - return personalized mix
        return getAuthenticatedPersonalizedFeed(hashtag, followedHubIds, pageNo, pageSize, sortBy, sortDir);
    }

    private List<Long> getFollowedHubIds(Long userId) {
        Set<Long> followedHubIdsSet = fanHubMemberRepository.findAllByUserId(userId).stream()
                .map(member -> member.getHub().getId())
                .collect(Collectors.toSet());

        fanHubRepository.findByOwnerUserIdAndIsActive(userId, true)
                .ifPresent(hub -> followedHubIdsSet.add(hub.getId()));

        return new ArrayList<>(followedHubIdsSet);
    }

    private List<PostResponse> getAuthenticatedPersonalizedFeed(String hashtag, List<Long> followedHubIds, int pageNo, int pageSize, String sortBy, String sortDir) {
        int totalNeeded = (pageNo + 1) * pageSize;

        // Fetch up to totalNeeded from each source to allow for full backfilling if one is empty
        Pageable fetchPageable = PageRequest.of(0, totalNeeded, Sort.by(getSortDirection(sortDir), sortBy));
        List<Post> followedPosts = postRepository.findByHubIdInAndStatusApproved(hashtag, followedHubIds, fetchPageable).getContent();

        List<String> followedCategories = postRepository.findCategoriesByHubIds(followedHubIds);
        List<Post> suggestionPosts = fetchSuggestionPosts(hashtag, followedHubIds, followedCategories, totalNeeded, sortBy, sortDir);

        // Merge posts maintaining ratio but filling gaps to ensure pageSize is met
        List<Post> mergedPosts = mergePostsWithBackfill(followedPosts, suggestionPosts, totalNeeded);

        return paginateMergedResults(mergedPosts, pageNo, pageSize);
    }

    private List<Post> fetchSuggestionPosts(String hashtag, List<Long> followedHubIds, List<String> followedCategories, int totalNeeded, String sortBy, String sortDir) {
        Pageable suggestionPageable = PageRequest.of(0, totalNeeded, Sort.by(getSortDirection(sortDir), sortBy));
        List<Post> suggestions = new ArrayList<>();

        if (!followedCategories.isEmpty()) {
            suggestions = postRepository.findPublicPostsByCategories(hashtag, followedHubIds, followedCategories, suggestionPageable).getContent();
        }

        // Fallback: If no category-based suggestions found (or no categories), get any public posts
        if (suggestions.isEmpty()) {
            suggestions = postRepository.findPublicPosts(hashtag, followedHubIds, suggestionPageable).getContent();
        }

        return suggestions;
    }

    private List<Post> mergePostsWithBackfill(List<Post> followed, List<Post> suggestions, int totalNeeded) {
        List<Post> merged = new ArrayList<>();
        int fIdx = 0;
        int sIdx = 0;
        Set<Long> seenIds = new HashSet<>();

        while (merged.size() < totalNeeded && (fIdx < followed.size() || sIdx < suggestions.size())) {
            boolean shouldPickSuggestion = false;
            if (sIdx < suggestions.size()) {
                if (fIdx >= followed.size()) {
                    shouldPickSuggestion = true;
                } else {
                    // Ratio-based pick: Aim for SUGGESTION_RATIO (0.3)
                    // If current ratio is below target, pick suggestion.
                    // Empty list starts with ratio 0, so pick suggestion first to guarantee "at least 1".
                    double currentRatio = merged.isEmpty() ? 0 : (double) sIdx / merged.size();
                    if (currentRatio < SUGGESTION_RATIO) {
                        shouldPickSuggestion = true;
                    }
                }
            }

            if (shouldPickSuggestion) {
                Post p = suggestions.get(sIdx++);
                if (seenIds.add(p.getId())) {
                    merged.add(p);
                }
            } else if (fIdx < followed.size()) {
                Post p = followed.get(fIdx++);
                if (seenIds.add(p.getId())) {
                    merged.add(p);
                }
            } else {
                break;
            }
        }
        return merged;
    }

    private List<PostResponse> paginateMergedResults(List<Post> mergedPosts, int pageNo, int pageSize) {
        int startIndex = pageNo * pageSize;
        int endIndex = Math.min(startIndex + pageSize, mergedPosts.size());

        if (startIndex >= mergedPosts.size()) {
            int totalPages = (int) Math.ceil((double) mergedPosts.size() / pageSize);
            if (totalPages == 0) return Collections.emptyList();
            startIndex = (totalPages - 1) * pageSize;
            endIndex = mergedPosts.size();
        }

        return mergedPosts.subList(startIndex, endIndex).stream()
                .map(this::mapToPostResponse)
                .collect(Collectors.toList());
    }

    @Override
    public TranslatePostResponse translatePost(Long postId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new NotFoundException("Post not found"));
        String translatingLanguage = "English";
        boolean userHasSetLanguage = false;

        String token = jwtService.getCurrentToken(httpServletRequest);
        String tokenUsername;
        Optional<User> tokenUser = Optional.empty();
        if (token != null) {
            try {
                tokenUsername = jwtService.getUsernameFromToken(token);
                tokenUser = userRepository.findByUsernameAndIsActive(tokenUsername);
            } catch (Exception e) {
                // Token is invalid or expired, treat as unauthenticated
                tokenUser = Optional.empty();
            }
        }

        if(tokenUser.isPresent()){
            String userSetLanguage = tokenUser.get().getTranslateLanguage();
            if(userSetLanguage != null){
                translatingLanguage = userSetLanguage;
                userHasSetLanguage = true;
            }
        }
        // the response will be in such format: TitleTranslation@ContentTranslation
        String translateResponse = geminiAIServiceImpl.translatePost(post.getContent(), post.getTitle(), translatingLanguage);

        // if the post's type is POLL, translate its options too
        List<String> listOptions = new ArrayList<>();
        if(post.getPostType().equals("POLL")){
            String translatedOptions = geminiAIServiceImpl.translatePostPollOptions(post.getId());
            String[] translatedOptionsSplit = translatedOptions.split("@");
            for(int i = 0 ; i < translatedOptionsSplit.length; i++){
                listOptions.add(translatedOptionsSplit[i]);
            }
        }

        String[] responseSplit = translateResponse.split("@");
        String translatedTitle = responseSplit[0];
        String translatedContent = "";
        if(responseSplit.length > 1){
            translatedContent = responseSplit[1];
        }

        return TranslatePostResponse.builder()
                .translatedTitle(translatedTitle)
                .translatedContent(translatedContent)
                .translateLanguageSet(userHasSetLanguage)
                .pollOptionsTranslation(listOptions)
                    .extraComment(!userHasSetLanguage ? "Set your preferred language in the settings!" : null)
                .build();
    }

    @Override
    public SummarizePostResponse summarizePost(Long postId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new NotFoundException("Post not found"));
        String summmaryLanguage = "English";

        String token = jwtService.getCurrentToken(httpServletRequest);
        String tokenUsername;
        Optional<User> tokenUser = Optional.empty();
        if (token != null) {
            try {
                tokenUsername = jwtService.getUsernameFromToken(token);
                tokenUser = userRepository.findByUsernameAndIsActive(tokenUsername);
            } catch (Exception e) {
                // Token is invalid or expired, treat as unauthenticated
                tokenUser = Optional.empty();
            }
        }

        if(tokenUser.isPresent()){
            String userSetLanguage = tokenUser.get().getTranslateLanguage();
            if(userSetLanguage != null){
                summmaryLanguage = userSetLanguage;
            }
        }

        return SummarizePostResponse.builder()
                .summarizeResult(geminiAIServiceImpl.summarizePost(post.getContent(), post.getTitle(), summmaryLanguage))
                .build();
    }

    private void setAuthorFrameDetails(User author, java.util.function.Consumer<java.math.BigDecimal> sizeSetter,
                                       java.util.function.Consumer<java.math.BigDecimal> xAxisSetter,
                                       java.util.function.Consumer<java.math.BigDecimal> yAxisSetter) {
        if (author.getFrameUrl() != null && !author.getFrameUrl().isEmpty()) {
            itemRepository.findByImageUrl(author.getFrameUrl()).ifPresent(item -> {
                sizeSetter.accept(item.getSize());
                xAxisSetter.accept(item.getXAxis());
                yAxisSetter.accept(item.getYAxis());
            });
        }
    }

    private PostResponse mapToPostResponse(Post post) {
        PostResponse response = new PostResponse();
        response.setPostId(post.getId());
        response.setFanHubId(post.getHub().getId());
        response.setFanHubName(post.getHub().getHubName());
        response.setFanHubSubdomain(post.getHub().getSubdomain());
        response.setAuthorId(post.getUser().getId());
        response.setAuthorUsername(post.getUser().getUsername());
        response.setAuthorDisplayName(post.getUser().getDisplayName());
        response.setAuthorAvatarUrl(post.getUser().getAvatarUrl());
        response.setAuthorFrameUrl(post.getUser().getFrameUrl());
        setAuthorFrameDetails(post.getUser(), response::setAuthorFrameSize, response::setAuthorFrameXAxis, response::setAuthorFrameYAxis);
        response.setPostType(post.getPostType());
        response.setTitle(post.getTitle());
        response.setContent(post.getContent());
        response.setStatus(post.getStatus());
        response.setIsPinned(post.getIsPinned());
        response.setCreatedAt(post.getCreatedAt());
        response.setUpdatedAt(post.getUpdatedAt());
        response.setStartTime(post.getStartTime());
        response.setEndTime(post.getEndTime());

        //Media URLs - using pre-fetched collection with @BatchSize
        List<String> mediaUrls = new ArrayList<>();
        if (post.getPostMedias() != null) {
            for (PostMedia media : post.getPostMedias()) {
                mediaUrls.add(media.getMediaUrl());
            }
        }
        response.setMediaUrls(mediaUrls);

        //Hashtags - using pre-fetched collection with @BatchSize
        List<String> hashtags = new ArrayList<>();
        if (post.getPostHashtags() != null) {
            for (PostHashtag hashtag : post.getPostHashtags()) {
                hashtags.add(hashtag.getHashtag());
            }
        }
        response.setHashtags(hashtags);

        //Vote option
        if ("POLL".equals(post.getPostType())) {
            List<VoteOptionResponse> optionResponses = new ArrayList<>();
            Map<Long, Long> voteCounts = new HashMap<>();
            Long totalVotes = 0L;

            if (post.getVoteOptions() != null) {
                for (VoteOption option : post.getVoteOptions()) {
                    optionResponses.add(new VoteOptionResponse(option.getId(), option.getOptionText()));
                    Long optionVoteCount = postVoteRepository.countByOptionId(option.getId());
                    voteCounts.put(option.getId(), optionVoteCount != null ? optionVoteCount : 0L);
                    totalVotes += optionVoteCount != null ? optionVoteCount : 0L;
                }
            }
            response.setVoteOptions(optionResponses);
            response.setVoteCounts(voteCounts);
            response.setTotalVotes(totalVotes);

            // Check if current user voted on this post
            try {
                User currentUser = authService.getUserFromToken(httpServletRequest);
                Long userVotedOptionId = null;
                if (post.getVoteOptions() != null) {
                    for (VoteOption option : post.getVoteOptions()) {
                        Optional<PostVote> userVote = postVoteRepository.findByUserIdAndOptionId(currentUser.getId(), option.getId());
                        if (userVote.isPresent()) {
                            userVotedOptionId = option.getId();
                            break;
                        }
                    }
                }
                response.setUserVotedOptionId(userVotedOptionId);
            } catch (Exception e) {
                response.setUserVotedOptionId(null);
            }
        }

        //Count like
        Long likeCount = postLikeRepository.countByPostId(post.getId());
        response.setLikeCount(likeCount);

        // Count comments
        Long commentCount = postCommentRepository.countByPostIdAndStatusNot(post.getId(), "DELETED");
        response.setCommentCount(commentCount);

        // Check if current user liked this post
        try {
            User currentUser = authService.getUserFromToken(httpServletRequest);
            Boolean isLiked = postLikeRepository.findByUserIdAndPostId(currentUser.getId(), post.getId()).isPresent();
            response.setIsLikedByCurrentUser(isLiked);
        } catch (Exception e) {
            response.setIsLikedByCurrentUser(false);
        }

        return response;
    }

    private PostWithMediaResponse mapToPostWithMediaResponse(Post post) {
        PostWithMediaResponse response = new PostWithMediaResponse();
        response.setPostId(post.getId());
        response.setFanHubId(post.getHub().getId());
        response.setFanHubName(post.getHub().getHubName());
        response.setFanHubSubdomain(post.getHub().getSubdomain());
        response.setAuthorId(post.getUser().getId());
        response.setAuthorUsername(post.getUser().getUsername());
        response.setAuthorDisplayName(post.getUser().getDisplayName());
        response.setAuthorAvatarUrl(post.getUser().getAvatarUrl());
        response.setAuthorFrameUrl(post.getUser().getFrameUrl());
        setAuthorFrameDetails(post.getUser(), response::setAuthorFrameSize, response::setAuthorFrameXAxis, response::setAuthorFrameYAxis);
        response.setPostType(post.getPostType());
        response.setTitle(post.getTitle());
        response.setContent(post.getContent());
        response.setStatus(post.getStatus());
        response.setIsPinned(post.getIsPinned());
        response.setCreatedAt(post.getCreatedAt());
        response.setUpdatedAt(post.getUpdatedAt());
        response.setStartTime(post.getStartTime());
        response.setEndTime(post.getEndTime());

        // Media with AI validation fields - using pre-fetched collection with @BatchSize
        List<PostWithMediaResponse.PostMediaItem> mediaItems = new ArrayList<>();
        if (post.getPostMedias() != null) {
            for (PostMedia media : post.getPostMedias()) {
                PostWithMediaResponse.PostMediaItem mediaItem = new PostWithMediaResponse.PostMediaItem();
                mediaItem.setMediaId(media.getId());
                mediaItem.setMediaUrl(media.getMediaUrl());
                mediaItem.setAiValidationStatus(media.getAiValidationStatus());
                mediaItem.setAiValidationComment(media.getAiValidationComment());
                mediaItems.add(mediaItem);
            }
        }
        response.setMedia(mediaItems);

        // Hashtags - using pre-fetched collection with @BatchSize
        List<String> hashtags = new ArrayList<>();
        if (post.getPostHashtags() != null) {
            for (PostHashtag hashtag : post.getPostHashtags()) {
                hashtags.add(hashtag.getHashtag());
            }
        }
        response.setHashtags(hashtags);

        // Vote option
        if ("POLL".equals(post.getPostType())) {
            List<VoteOptionResponse> optionResponses = new ArrayList<>();
            Map<Long, Long> voteCounts = new HashMap<>();
            Long totalVotes = 0L;

            if (post.getVoteOptions() != null) {
                for (VoteOption option : post.getVoteOptions()) {
                    optionResponses.add(new VoteOptionResponse(option.getId(), option.getOptionText()));
                    Long optionVoteCount = postVoteRepository.countByOptionId(option.getId());
                    voteCounts.put(option.getId(), optionVoteCount != null ? optionVoteCount : 0L);
                    totalVotes += optionVoteCount != null ? optionVoteCount : 0L;
                }
            }
            response.setVoteOptions(optionResponses);
            response.setVoteCounts(voteCounts);
            response.setTotalVotes(totalVotes);

            // Check if current user voted on this post
            try {
                User currentUser = authService.getUserFromToken(httpServletRequest);
                Long userVotedOptionId = null;
                if (post.getVoteOptions() != null) {
                    for (VoteOption option : post.getVoteOptions()) {
                        Optional<PostVote> userVote = postVoteRepository.findByUserIdAndOptionId(currentUser.getId(), option.getId());
                        if (userVote.isPresent()) {
                            userVotedOptionId = option.getId();
                            break;
                        }
                    }
                }
                response.setUserVotedOptionId(userVotedOptionId);
            } catch (Exception e) {
                response.setUserVotedOptionId(null);
            }
        }

        // Count like
        Long likeCount = postLikeRepository.countByPostId(post.getId());
        response.setLikeCount(likeCount);

        // Check if current user liked this post
        try {
            User currentUser = authService.getUserFromToken(httpServletRequest);
            Boolean isLiked = postLikeRepository.findByUserIdAndPostId(currentUser.getId(), post.getId()).isPresent();
            response.setIsLikedByCurrentUser(isLiked);
        } catch (Exception e) {
            response.setIsLikedByCurrentUser(false);
        }

        // AI Validation fields (Post level)
        response.setAiValidationStatus(post.getFinalAiValidationStatus());
        response.setAiValidationComment(post.getAiValidationComment());
        response.setAiContentValidationStatus(post.getContentAiValidationStatus());

        return response;
    }

    @Override
    @Transactional
    public String likePost(Long postId) {
        User currentUser = authService.getUserFromToken(httpServletRequest);

        Optional<Post> post = postRepository.findById(postId);
        if (post.isEmpty()) {
            throw new NotFoundException("Post not found");
        }

        Long userId = currentUser.getId();

        // Check if user already liked this post
        Optional<PostLike> existingLike = postLikeRepository.findByUserIdAndPostId(userId, postId);
        if (existingLike.isPresent()) {
            throw new IllegalArgumentException("You have already liked this post");
        }

        // Create and save the like
        PostLike postLike = new PostLike();
        postLike.setUser(currentUser);
        postLike.setPost(post.get());
        postLike.setCreatedAt(Instant.now());
        postLikeRepository.save(postLike);

        // Update user track
        userTrackService.updateOnLike(currentUser);

        Optional<UserDailyMission> userDailyMission = userDailyMissionRepository.findById(userId);
        if (userDailyMission.isPresent()) {
            UserDailyMission mission = userDailyMission.get();
            int newLikeAmount = mission.getLikeAmount() + 1;
            mission.setLikeAmount(newLikeAmount);
            userDailyMissionRepository.save(mission);
            
            // Award points based on daily mission milestones
            userDailyMissionService.awardPointsForLikes(userId, newLikeAmount);
        } else {
            throw new NotFoundException("User daily mission not found");
        }

        // Send SSE notification to post author about the new like
        // Only send if the liker is not the post author themselves
        // Also persists notification to database
        User postAuthor = post.get().getUser();
        if (!postAuthor.getId().equals(currentUser.getId())) {
            notificationService.sendPostLikeNotification(
                    postAuthor.getId(),
                    currentUser.getId(),
                    currentUser.getUsername(),
                    currentUser.getAvatarUrl(),
                    currentUser.getFrameUrl(),
                    postId,
                    post.get().getTitle(),
                    post.get().getHub().getId(),
                    post.get().getHub().getHubName()
            );
            log.info("Sent SSE notification to post author {} for like from user {}",
                    postAuthor.getId(), currentUser.getId());
        }

        return "Post liked successfully!";
    }

    @Override
    @Transactional
    public String unlikePost(Long postId) {
        User currentUser = authService.getUserFromToken(httpServletRequest);

        Optional<Post> post = postRepository.findById(postId);
        if (post.isEmpty()) {
            throw new NotFoundException("Post not found");
        }

        Long userId = currentUser.getId();

        Optional<PostLike> existingLike = postLikeRepository.findByUserIdAndPostId(userId, postId);
        if (existingLike.isEmpty()) {
            throw new IllegalArgumentException("You have not liked this post");
        }

        postLikeRepository.delete(existingLike.get());

        return "Post unliked successfully. 10 points deducted.";
    }

    @Override
    @Transactional
    public String pinPost(Long postId) {
        User currentUser = authService.getUserFromToken(httpServletRequest);

        Optional<Post> post = postRepository.findById(postId);
        if (post.isEmpty()) {
            throw new NotFoundException("Post not found");
        }

        Long fanHubId = post.get().getHub().getId();

        boolean isOwner = "VTUBER".equals(currentUser.getRole()) &&
                fanHubRepository.findById(fanHubId)
                        .map(hub -> hub.getOwnerUser().getId().equals(currentUser.getId()))
                        .orElse(false);

        boolean isModerator = fanHubMemberRepository.findByHubIdAndUserId(fanHubId, currentUser.getId())
                .map(member -> "MODERATOR".equals(member.getRoleInHub()))
                .orElse(false);

        if (!isOwner && !isModerator) {
            throw new AccessDeniedException("Only VTUBER (owner) or MODERATOR can pin posts");
        }

        post.get().setIsPinned(true);
        post.get().setUpdatedAt(Instant.now());
        postRepository.save(post.get());

        return "Post pinned successfully";
    }

    @Override
    @Transactional
    public String unpinPost(Long postId) {
        User currentUser = authService.getUserFromToken(httpServletRequest);

        Optional<Post> post = postRepository.findById(postId);
        if (post.isEmpty()) {
            throw new NotFoundException("Post not found");
        }

        Long fanHubId = post.get().getHub().getId();

        boolean isOwner = "VTUBER".equals(currentUser.getRole()) &&
                fanHubRepository.findById(fanHubId)
                        .map(hub -> hub.getOwnerUser().getId().equals(currentUser.getId()))
                        .orElse(false);

        boolean isModerator = fanHubMemberRepository.findByHubIdAndUserId(fanHubId, currentUser.getId())
                .map(member -> "MODERATOR".equals(member.getRoleInHub()))
                .orElse(false);

        if (!isOwner && !isModerator) {
            throw new AccessDeniedException("Only VTUBER (owner) or MODERATOR can unpin posts");
        }

        post.get().setIsPinned(false);
        post.get().setUpdatedAt(Instant.now());
        postRepository.save(post.get());

        return "Post unpinned successfully";
    }

    @Override
    @Transactional
    public String votePost(Long postId, Long optionId) {
        User currentUser = authService.getUserFromToken(httpServletRequest);

        Optional<Post> post = postRepository.findById(postId);
        if (post.isEmpty()) {
            throw new NotFoundException("Post not found");
        }

        if (!"POLL".equals(post.get().getPostType())) {
            throw new IllegalArgumentException("This post is not a poll");
        }

        Optional<VoteOption> option = voteOptionRepository.findById(optionId);
        if (option.isEmpty()) {
            throw new NotFoundException("Vote option not found");
        }

        if (!option.get().getPost().getId().equals(postId)) {
            throw new IllegalArgumentException("Vote option does not belong to this post");
        }

        Long userId = currentUser.getId();

        // Check if user already voted on this post (any option)
        List<PostVote> existingVotes = postVoteRepository.findByUserIdAndPostId(userId, postId);
        
        if (!existingVotes.isEmpty()) {
            // User already voted - update their vote to the new option
            PostVote existingVote = existingVotes.get(0);
            Long oldOptionId = existingVote.getOption().getId();
            
            if (oldOptionId.equals(optionId)) {
                return "You have already voted on this option";
            }
            
            // Update the vote to the new option
            existingVote.setOption(option.get());
            existingVote.setVotedAt(Instant.now());
            postVoteRepository.save(existingVote);
            
            return "Vote changed successfully!";
        }

        // Create and save the new vote
        PostVote postVote = new PostVote();
        postVote.setUser(currentUser);
        postVote.setOption(option.get());
        postVote.setVotedAt(Instant.now());
        postVoteRepository.save(postVote);

        return "Vote submitted successfully!";
    }

    @Override
    @Transactional
    public String unVotePost(Long postId) {
        User currentUser = authService.getUserFromToken(httpServletRequest);

        Optional<Post> post = postRepository.findById(postId);
        if (post.isEmpty()) {
            throw new NotFoundException("Post not found");
        }

        if (!"POLL".equals(post.get().getPostType())) {
            throw new IllegalArgumentException("This post is not a poll");
        }

        Long userId = currentUser.getId();

        // Find user's vote on this post
        List<VoteOption> allOptions = voteOptionRepository.findAllByPostId(postId);
        PostVote existingVote = null;
        for (VoteOption option : allOptions) {
            Optional<PostVote> vote = postVoteRepository.findByUserIdAndOptionId(userId, option.getId());
            if (vote.isPresent()) {
                existingVote = vote.get();
                break;
            }
        }

        if (existingVote == null) {
            throw new IllegalArgumentException("You have not voted on this post");
        }

        postVoteRepository.delete(existingVote);

        return "Vote removed successfully!";
    }

    @Override
    @Transactional
    public String deletePost(Long postId) {
        User currentUser = authService.getUserFromToken(httpServletRequest);

        Optional<Post> post = postRepository.findById(postId);
        if (post.isEmpty()) {
            throw new NotFoundException("Post not found");
        }

        Long fanHubId = post.get().getHub().getId();

        // Check if current user is the post author
        boolean isAuthor = post.get().getUser().getId().equals(currentUser.getId());

        // Check if user is VTUBER and owns this FanHub
        boolean isOwner = "VTUBER".equals(currentUser.getRole()) &&
                fanHubRepository.findById(fanHubId)
                        .map(hub -> hub.getOwnerUser().getId().equals(currentUser.getId()))
                        .orElse(false);

        // Check if user is a member with MODERATOR role
        boolean isModerator = fanHubMemberRepository.findByHubIdAndUserId(fanHubId, currentUser.getId())
                .map(member -> "MODERATOR".equals(member.getRoleInHub()))
                .orElse(false);

        if (!isAuthor && !isOwner && !isModerator) {
            throw new AccessDeniedException("Only the post author, VTUBER (owner), or MODERATOR can delete this post");
        }

        post.get().setStatus("DELETED");
        post.get().setUpdatedAt(Instant.now());
        postRepository.save(post.get());

        return "Post deleted successfully";
    }

    @Override
    @Transactional(readOnly = true)
    public List<PostWithMediaResponse> getBookmarkPosts(int pageNo, int pageSize, String sortBy, String sortDir) {
        User currentUser = authService.getUserFromToken(httpServletRequest);

        Pageable paging = PageRequest.of(pageNo, pageSize, Sort.by(getSortDirection(sortDir), sortBy));

        Page<UserBookmark> pagedBookmarks = userBookmarkRepository.findByUserId(currentUser.getId(), paging);

        if (pagedBookmarks.isEmpty()) {
            return List.of();
        }

        return pagedBookmarks.getContent().stream()
                .map(bookmark -> mapToPostWithMediaResponse(bookmark.getPost()))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<PostWithMediaResponse> getPostsByUsername(int pageNo, int pageSize, String sortBy, String sortDir) {
        User currentUser = authService.getUserFromToken(httpServletRequest);

        // Find user by username
        Optional<User> user = userRepository.findByUsernameAndIsActive(currentUser.getUsername());
        if (user.isEmpty()) {
            throw new NotFoundException("User not found");
        }

        Pageable paging = PageRequest.of(pageNo, pageSize, Sort.by(getSortDirection(sortDir), sortBy));

        Page<Post> pagedPosts = postRepository.findByUsername(currentUser.getUsername(), paging);

        if (pagedPosts.isEmpty()) {
            return List.of();
        }

        return pagedPosts.getContent().stream()
                .map(this::mapToPostWithMediaResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<PostWithMediaResponse> getAllPostsByFanHubId(Long fanHubId, int pageNo, int pageSize, String sortBy, String sortDir) {
        User currentUser = authService.getUserFromToken(httpServletRequest);

        Optional<FanHub> fanHub = fanHubRepository.findById(fanHubId);
        if (fanHub.isEmpty()) {
            throw new NotFoundException("FanHub not found");
        }

        // Check if user is VTUBER and owns this FanHub
        boolean isOwner = "VTUBER".equals(currentUser.getRole()) &&
                fanHub.get().getOwnerUser().getId().equals(currentUser.getId());

        // Check if user is a member with MODERATOR role
        boolean isModerator = fanHubMemberRepository.findByHubIdAndUserId(fanHubId, currentUser.getId())
                .map(member -> "MODERATOR".equals(member.getRoleInHub()))
                .orElse(false);

        if (!isOwner && !isModerator) {
            throw new AccessDeniedException("Only VTUBER (owner) or MODERATOR can view all posts");
        }

        Pageable paging = PageRequest.of(pageNo, pageSize, Sort.by(getSortDirection(sortDir), sortBy));

        Page<Post> pagedPosts = postRepository.findByHubIdAndStatusNotDeleted(fanHubId, paging);

        if (pagedPosts.isEmpty()) {
            return List.of();
        }

        return pagedPosts.getContent().stream()
                .map(this::mapToPostWithMediaResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<PostResponse> getAllPostsBySubdomain(String subdomain, int pageNo, int pageSize, String sortBy, String sortDir) {
        User currentUser = authService.getUserFromToken(httpServletRequest);

        Optional<FanHub> fanHub = fanHubRepository.findBySubdomainAndIsActive(subdomain, true);
        if (fanHub.isEmpty()) {
            throw new NotFoundException("FanHub not found");
        }

        // Check if user is VTUBER and owns this FanHub
        boolean isOwner = "VTUBER".equals(currentUser.getRole()) &&
                fanHub.get().getOwnerUser().getId().equals(currentUser.getId());

        // Check if user is a member with MODERATOR role
        boolean isModerator = fanHubMemberRepository.findByHubIdAndUserId(fanHub.get().getId(), currentUser.getId())
                .map(member -> "MODERATOR".equals(member.getRoleInHub()))
                .orElse(false);

        if (!isOwner && !isModerator) {
            throw new AccessDeniedException("Only VTUBER (owner) or MODERATOR can view all posts");
        }

        Pageable paging = PageRequest.of(pageNo, pageSize, Sort.by(getSortDirection(sortDir), sortBy));

        Page<Post> pagedPosts = postRepository.findByHubIdAndStatusNotDeleted(fanHub.get().getId(), paging);

        if (pagedPosts.isEmpty()) {
            return List.of();
        }

        return pagedPosts.getContent().stream()
                .map(this::mapToPostResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public PostResponse getPostDetail(Long postId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new NotFoundException("Post not found"));

        return mapToPostResponse(post);
    }

    @Override
    @Transactional(readOnly = true)
    public PostResponse getApprovedPostById(Long postId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new NotFoundException("Post not found"));

        if (!"APPROVED".equals(post.getStatus())) {
            throw new NotFoundException("Post not found");
        }

        return mapToPostResponse(post);
    }

    @Override
    @Transactional(readOnly = true)
    public List<PostResponse> getTrendingPostsByFanHub(Long fanHubId, int pageNo, int pageSize, String sortBy, String sortDir) {
        Optional<FanHub> fanHub = fanHubRepository.findById(fanHubId);
        if (fanHub.isEmpty()) {
            throw new NotFoundException("FanHub not found");
        }

        if (fanHub.get().getIsPrivate()) {
            throw new AccessDeniedException("This FanHub is private");
        }

        Pageable paging = PageRequest.of(pageNo, pageSize, Sort.by(getSortDirection(sortDir), sortBy));

        Page<Post> pagedPosts = postRepository.findTrendingPostsByFanHub(fanHubId, paging);

        if (pagedPosts.isEmpty()) {
            return List.of();
        }

        return pagedPosts.getContent().stream()
                .map(this::mapToPostResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public PostResponse getTrendingPublicPost() {
        Instant oneDayAgo = Instant.now().minus(24, ChronoUnit.HOURS);
        // Fetch top 10 trending posts to rotate
        List<Post> trendingPosts = postRepository.findTrendingPost(oneDayAgo, PageRequest.of(0, 10));

        if (trendingPosts.isEmpty()) {
            throw new NotFoundException("No public posts available");
        }

        Long nextIndex = redisTemplate.opsForValue().increment(TRENDING_POST_INDEX_KEY);
        int indexToUse = (int) (nextIndex % trendingPosts.size());

        return mapToPostResponse(trendingPosts.get(indexToUse));
    }

    @Override
    @Transactional(readOnly = true)
    public PostResponse getLatestPublicApprovedPost() {
        // Fetch top 10 latest posts to rotate
        List<Post> posts = postRepository.findLatestPublicApprovedPost(PageRequest.of(0, 10));

        if (posts.isEmpty()) {
            throw new NotFoundException("No public posts available");
        }

        Long nextIndex = redisTemplate.opsForValue().increment(LATEST_POST_INDEX_KEY);
        int indexToUse = (int) (nextIndex % posts.size());

        return mapToPostResponse(posts.get(indexToUse));
    }

    @Override
    @Transactional(readOnly = true)
    public List<PostResponse> searchPosts(String keyword, int pageNo, int pageSize, String sortBy, String sortDir) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return List.of();
        }

        Pageable paging = PageRequest.of(pageNo, pageSize, Sort.by(getSortDirection(sortDir), sortBy));

        Page<Post> pagedPosts = postRepository.searchPosts(keyword.trim(), paging);

        if (pagedPosts.isEmpty()) {
            return List.of();
        }

        return pagedPosts.getContent().stream()
                .map(this::mapToPostResponse)
                .collect(Collectors.toList());
    }

    @Override
    public long countPendingPostsByFanHubId(Long fanHubId) {
        return postRepository.countByHubIdAndStatus(fanHubId, "PENDING");
    }

    private Sort.Direction getSortDirection(String sortDir) {
        if (sortDir != null && sortDir.equalsIgnoreCase("asc")) {
            return Sort.Direction.ASC;
        }
        return Sort.Direction.DESC;
    }
}
