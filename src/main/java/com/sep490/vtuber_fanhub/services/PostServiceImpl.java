package com.sep490.vtuber_fanhub.services;

import com.sep490.vtuber_fanhub.dto.requests.CreatePostRequest;
import com.sep490.vtuber_fanhub.exceptions.CustomAuthenticationException;
import com.sep490.vtuber_fanhub.exceptions.NotFoundException;
import com.sep490.vtuber_fanhub.models.FanHub;
import com.sep490.vtuber_fanhub.models.FanHubMember;
import com.sep490.vtuber_fanhub.models.Post;
import com.sep490.vtuber_fanhub.models.PostHashtag;
import com.sep490.vtuber_fanhub.models.PostMedia;
import com.sep490.vtuber_fanhub.models.User;
import com.sep490.vtuber_fanhub.repositories.FanHubMemberRepository;
import com.sep490.vtuber_fanhub.repositories.FanHubRepository;
import com.sep490.vtuber_fanhub.repositories.PostHashtagRepository;
import com.sep490.vtuber_fanhub.repositories.PostMediaRepository;
import com.sep490.vtuber_fanhub.repositories.PostRepository;
import com.sep490.vtuber_fanhub.repositories.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

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

        // Upload media if needed
        try {
            if ("IMAGE".equals(postType) && images != null) {
                for (MultipartFile image : images) {
                    // Resize images if they are large (max 1920x1080)
                    String imageUrl = cloudinaryService.uploadImage(image, true, 1920, 1080);
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
}
