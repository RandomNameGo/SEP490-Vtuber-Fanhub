package com.sep490.vtuber_fanhub.services;

import com.sep490.vtuber_fanhub.dto.requests.CreatePostRequest;
import com.sep490.vtuber_fanhub.dto.responses.PostResponse;
import com.sep490.vtuber_fanhub.exceptions.CustomAuthenticationException;
import com.sep490.vtuber_fanhub.exceptions.NotFoundException;
import com.sep490.vtuber_fanhub.models.*;
import com.sep490.vtuber_fanhub.repositories.FanHubMemberRepository;
import com.sep490.vtuber_fanhub.repositories.FanHubRepository;
import com.sep490.vtuber_fanhub.repositories.PostHashtagRepository;
import com.sep490.vtuber_fanhub.repositories.PostMediaRepository;
import com.sep490.vtuber_fanhub.repositories.PostRepository;
import com.sep490.vtuber_fanhub.repositories.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PostServiceImpl implements PostService {

    private final PostRepository postRepository;

    private final PostHashtagRepository postHashtagRepository;

    private final PostMediaRepository postMediaRepository;

    private final FanHubRepository fanHubRepository;

    private final FanHubMemberRepository fanHubMemberRepository;

    private final UserRepository userRepository;

    private final JWTService jwtService;

    private final HttpServletRequest httpServletRequest;

    private final CloudinaryService cloudinaryService;

    @Override
    @Transactional
    public String createPost(CreatePostRequest request, List<MultipartFile> images, MultipartFile video) {
        String token = jwtService.getCurrentToken(httpServletRequest);
        String tokenUsername = jwtService.getUsernameFromToken(token);

        Optional<User> tokenUser = userRepository.findByUsernameAndIsActive(tokenUsername);
        if (tokenUser.isEmpty()) {
            throw new CustomAuthenticationException("Authentication failed");
        }

        Optional<FanHub> fanHub = fanHubRepository.findById(request.getFanHubId());
        if (fanHub.isEmpty()) {
            throw new NotFoundException("FanHub not found");
        }

        // Check if user is a member of the FanHub
        Optional<FanHubMember> member = fanHubMemberRepository.findByHubIdAndUserId(
                request.getFanHubId(), tokenUser.get().getId());
        if (member.isEmpty()) {
            throw new AccessDeniedException("You must be a member of this FanHub to create a post");
        }

        // Validate post type
        String postType = request.getPostType().toUpperCase();
        if (!List.of("TEXT", "IMAGE", "VIDEO").contains(postType)) {
            throw new IllegalArgumentException("Invalid post type. Must be TEXT, IMAGE, or VIDEO");
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

        // Create the post
        Post post = new Post();
        post.setHub(fanHub.get());
        post.setUser(tokenUser.get());
        post.setPostType(postType);
        post.setTitle(request.getTitle());
        post.setContent(request.getContent());
        post.setIsPinned(false);
        post.setStatus("PENDING"); // Default status is PENDING
        post.setCreatedAt(Instant.now());
        post.setUpdatedAt(Instant.now());

        postRepository.save(post);

        try {
            if ("IMAGE".equals(postType) && images != null) {
                for (MultipartFile image : images) {
                    // Resize images if they are large (max 1920x1080)
                    String imageUrl = cloudinaryService.uploadFile(image);
                    PostMedia postMedia = new PostMedia();
                    postMedia.setPost(post);
                    postMedia.setMediaUrl(imageUrl);
                    postMediaRepository.save(postMedia);
                }
            }

            if ("VIDEO".equals(postType) && video != null) {
                String videoUrl = cloudinaryService.uploadVideo(video);
                PostMedia postMedia = new PostMedia();
                postMedia.setPost(post);
                postMedia.setMediaUrl(videoUrl);
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

        return "Created post successfully";
    }

    @Override
    @Transactional(readOnly = true)
    public List<PostResponse> getPendingPosts(Long fanHubId, int pageNo, int pageSize, String sortBy) {
        String token = jwtService.getCurrentToken(httpServletRequest);
        String tokenUsername = jwtService.getUsernameFromToken(token);

        Optional<User> tokenUser = userRepository.findByUsernameAndIsActive(tokenUsername);
        if (tokenUser.isEmpty()) {
            throw new CustomAuthenticationException("Authentication failed");
        }

        Optional<FanHub> fanHub = fanHubRepository.findById(fanHubId);
        if (fanHub.isEmpty()) {
            throw new NotFoundException("FanHub not found");
        }

        // Check if user is VTUBER and owns this FanHub
        boolean isOwner = "VTUBER".equals(tokenUser.get().getRole()) &&
                fanHub.get().getOwnerUser().getId().equals(tokenUser.get().getId());

        // Check if user is a member with MODERATOR role
        boolean isModerator = fanHubMemberRepository.findByHubIdAndUserId(fanHubId, tokenUser.get().getId())
                .map(member -> "MODERATOR".equals(member.getRoleInHub()))
                .orElse(false);

        if (!isOwner && !isModerator) {
            throw new AccessDeniedException("Only VTUBER (owner) or MODERATOR can view pending posts");
        }

        Pageable paging = PageRequest.of(pageNo, pageSize, Sort.by(sortBy));

        Page<Post> pagedPosts = postRepository.findByHubIdAndStatus(fanHubId, "PENDING", paging);

        if (pagedPosts.isEmpty()) {
            return List.of();
        }

        return pagedPosts.getContent().stream()
                .map(this::mapToPostResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<PostResponse> getPosts(Long fanHubId, int pageNo, int pageSize, String sortBy, String postHashtag) {
        String token = jwtService.getCurrentToken(httpServletRequest);
        String tokenUsername = jwtService.getUsernameFromToken(token);

        Optional<User> tokenUser = userRepository.findByUsernameAndIsActive(tokenUsername);
        if (tokenUser.isEmpty()) {
            throw new CustomAuthenticationException("Authentication failed");
        }

        Optional<FanHub> fanHub = fanHubRepository.findById(fanHubId);
        if (fanHub.isEmpty()) {
            throw new NotFoundException("FanHub not found");
        }

        // Check if user is a member of the FanHub
        Optional<FanHubMember> member = fanHubMemberRepository.findByHubIdAndUserId(
                fanHubId, tokenUser.get().getId());
        if (member.isEmpty()) {
            // If fanHub is public, allow viewing posts
            if (!fanHub.get().getIsPrivate()) {
                // Continue - public fanHub, non-member can view approved posts
            } else {
                throw new AccessDeniedException("You must be a member of this FanHub to view posts");
            }
        }

        Pageable paging = PageRequest.of(pageNo, pageSize, Sort.by(sortBy));

        Page<Post> pagedPosts;
        if (postHashtag != null && !postHashtag.isEmpty()) {
            pagedPosts = postRepository.findByHubIdAndStatusAndHashtag(fanHubId, "APPROVED", postHashtag, paging);
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
    @Transactional
    public String reviewPost(Long postId, String status) {
        String token = jwtService.getCurrentToken(httpServletRequest);
        String tokenUsername = jwtService.getUsernameFromToken(token);

        Optional<User> tokenUser = userRepository.findByUsernameAndIsActive(tokenUsername);
        if (tokenUser.isEmpty()) {
            throw new CustomAuthenticationException("Authentication failed");
        }

        Optional<Post> post = postRepository.findById(postId);
        if (post.isEmpty()) {
            throw new NotFoundException("Post not found");
        }

        // Validate status parameter
        String normalizedStatus = status.toUpperCase();
        if (!List.of("APPROVED", "REJECTED").contains(normalizedStatus)) {
            throw new IllegalArgumentException("Invalid status. Must be APPROVED or REJECTED");
        }

        Long fanHubId = post.get().getHub().getId();

        // Check if user is VTUBER and owns this FanHub
        boolean isOwner = "VTUBER".equals(tokenUser.get().getRole()) &&
                fanHubRepository.findById(fanHubId)
                        .map(hub -> hub.getOwnerUser().getId().equals(tokenUser.get().getId()))
                        .orElse(false);

        // Check if user is a member with MODERATOR role
        boolean isModerator = fanHubMemberRepository.findByHubIdAndUserId(fanHubId, tokenUser.get().getId())
                .map(member -> "MODERATOR".equals(member.getRoleInHub()))
                .orElse(false);

        if (!isOwner && !isModerator) {
            throw new AccessDeniedException("Only VTUBER (owner) or MODERATOR can review posts");
        }

        // Update post status
        post.get().setStatus(normalizedStatus);
        post.get().setUpdatedAt(Instant.now());
        postRepository.save(post.get());

        return "Post " + normalizedStatus.toLowerCase() + " successfully";
    }

    @Override
    public Boolean AIValidate(Long postId) {
        return null;
    }

    private PostResponse mapToPostResponse(Post post) {
        PostResponse response = new PostResponse();
        response.setPostId(post.getId());
        response.setFanHubId(post.getHub().getId());
        response.setFanHubName(post.getHub().getHubName());
        response.setAuthorId(post.getUser().getId());
        response.setAuthorUsername(post.getUser().getUsername());
        response.setAuthorDisplayName(post.getUser().getDisplayName());
        response.setAuthorAvatarUrl(post.getUser().getAvatarUrl());
        response.setPostType(post.getPostType());
        response.setTitle(post.getTitle());
        response.setContent(post.getContent());
        response.setStatus(post.getStatus());
        response.setIsPinned(post.getIsPinned());
        response.setCreatedAt(post.getCreatedAt());
        response.setUpdatedAt(post.getUpdatedAt());

        // Get media URLs
        List<PostMedia> mediaList = postMediaRepository.findByPostId(post.getId());
        List<String> mediaUrls = new ArrayList<>();
        for (PostMedia media : mediaList) {
            mediaUrls.add(media.getMediaUrl());
        }
        response.setMediaUrls(mediaUrls);

        // Get hashtags
        List<PostHashtag> hashtagList = postHashtagRepository.findByPostId(post.getId());
        List<String> hashtags = new ArrayList<>();
        for (PostHashtag hashtag : hashtagList) {
            hashtags.add(hashtag.getHashtag());
        }
        response.setHashtags(hashtags);

        return response;
    }
}
