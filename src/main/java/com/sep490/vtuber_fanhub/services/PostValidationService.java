package com.sep490.vtuber_fanhub.services;

import com.sep490.vtuber_fanhub.exceptions.NotFoundException;
import com.sep490.vtuber_fanhub.models.Post;
import com.sep490.vtuber_fanhub.repositories.PostRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PostValidationService {

    private final PostRepository postRepository;

    @Async("validationExecutor")
    public void validatePost(Long postId) {

        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new NotFoundException("Post not found"));

        try {
            // do something idk
        } catch (Exception ermWhatTheSigma) {

        }

    }

    @Async("validationExecutor")
    public void validatePost(Post post) {

        try {
            // do something idk
        } catch (Exception ermWhatTheSigma) {

        }

    }
}
