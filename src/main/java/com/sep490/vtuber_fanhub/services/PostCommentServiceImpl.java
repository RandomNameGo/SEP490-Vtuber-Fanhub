package com.sep490.vtuber_fanhub.services;

import com.sep490.vtuber_fanhub.dto.requests.CreatePostCommentRequest;
import com.sep490.vtuber_fanhub.dto.responses.PostCommentResponse;
import com.sep490.vtuber_fanhub.exceptions.NotFoundException;
import com.sep490.vtuber_fanhub.models.Post;
import com.sep490.vtuber_fanhub.models.PostComment;
import com.sep490.vtuber_fanhub.models.PostCommentGift;
import com.sep490.vtuber_fanhub.models.User;
import com.sep490.vtuber_fanhub.repositories.PostCommentGiftRepository;
import com.sep490.vtuber_fanhub.repositories.PostCommentLikeRepository;
import com.sep490.vtuber_fanhub.repositories.PostCommentRepository;
import com.sep490.vtuber_fanhub.repositories.PostRepository;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PostCommentServiceImpl implements PostCommentService {

    private final PostCommentRepository postCommentRepository;

    private final PostRepository postRepository;

    private final HttpServletRequest httpServletRequest;

    private final AuthService authService;

    private final PostCommentLikeRepository postCommentLikeRepository;

    private final PostCommentGiftRepository postCommentGiftRepository;

    @Override
    @Transactional
    public boolean createPostComment(CreatePostCommentRequest createPostCommentRequest) {
        User currentUser = authService.getUserFromToken(httpServletRequest);

        Optional<Post> post = postRepository.findById(createPostCommentRequest.getPostId());
        if (post.isEmpty()) {
            throw new NotFoundException("Post not found");
        }

        PostComment postComment = new PostComment();
        postComment.setPost(post.get());
        postComment.setUser(currentUser);
        postComment.setContent(createPostCommentRequest.getContent());
        postComment.setStatus("VISIBLE");
        postComment.setCreatedAt(Instant.now());

        if (createPostCommentRequest.getParentCommentId() != null) {
            Optional<PostComment> parentComment = postCommentRepository.findById(createPostCommentRequest.getParentCommentId());
            parentComment.ifPresent(postComment::setParentComment);
        }

        postCommentRepository.save(postComment);
        return true;
    }

    @Override
    @Transactional(readOnly = true)
    public List<PostCommentResponse> getPostCommentsByPostId(Long postId) {
        Optional<Post> post = postRepository.findById(postId);
        if (post.isEmpty()) {
            throw new NotFoundException("Post not found");
        }

        List<PostComment> comments = postCommentRepository.findByPostIdOrderByCreatedAtAsc(postId);

        return comments.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    private PostCommentResponse mapToResponse(PostComment comment) {
        PostCommentResponse response = new PostCommentResponse();
        response.setCommentId(comment.getId());
        response.setPostId(comment.getPost().getId());

        response.setUserId(comment.getUser().getId());
        response.setUsername(comment.getUser().getUsername());
        response.setDisplayName(comment.getUser().getDisplayName());
        response.setAvatarUrl(comment.getUser().getAvatarUrl());

        response.setContent(comment.getContent());
        response.setStatus(comment.getStatus());
        response.setCreatedAt(comment.getCreatedAt());

        if (comment.getParentComment() != null) {
            response.setParentCommentId(comment.getParentComment().getId());
        }

        // Get like count
        Long likeCount = postCommentLikeRepository.countByComment(comment);
        response.setLikeCount(likeCount);

        // Check if current user liked this comment
        try {
            User currentUser = authService.getUserFromToken(httpServletRequest);
            Boolean isLiked = postCommentLikeRepository.findByUserIdAndComment(currentUser.getId(), comment).isPresent();
            response.setIsLikedByCurrentUser(isLiked);
        } catch (Exception e) {
            response.setIsLikedByCurrentUser(false);
        }

        // Get gift count for this comment
        List<PostCommentGift> gifts = postCommentGiftRepository.findByComment(comment);
        response.setGiftCount((long) gifts.size());

        return response;
    }
}
