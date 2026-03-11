package com.sep490.vtuber_fanhub.services;

import com.sep490.vtuber_fanhub.dto.requests.CreatePostCommentRequest;
import com.sep490.vtuber_fanhub.exceptions.NotFoundException;
import com.sep490.vtuber_fanhub.models.Post;
import com.sep490.vtuber_fanhub.models.PostComment;
import com.sep490.vtuber_fanhub.models.User;
import com.sep490.vtuber_fanhub.repositories.PostCommentRepository;
import com.sep490.vtuber_fanhub.repositories.PostRepository;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class PostCommentServiceImpl implements PostCommentService {

    private final PostCommentRepository postCommentRepository;

    private final PostRepository postRepository;

    private final HttpServletRequest httpServletRequest;

    private final AuthService authService;

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
}
