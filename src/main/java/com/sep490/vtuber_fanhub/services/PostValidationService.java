package com.sep490.vtuber_fanhub.services;

import com.sep490.vtuber_fanhub.exceptions.NotFoundException;
import com.sep490.vtuber_fanhub.models.Post;
import com.sep490.vtuber_fanhub.models.PostMedia;
import com.sep490.vtuber_fanhub.repositories.PostMediaRepository;
import com.sep490.vtuber_fanhub.repositories.PostRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PostValidationService {

    private final PostRepository postRepository;
    private final PostMediaRepository mediaRepository;
    private final ContentValidationService contentValidationService;

    @Async("validationExecutor")
    public void validatePost(Long postId) {

        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new NotFoundException("Post not found"));
        validatePost(post);
    }

    @Async("validationExecutor")
    public void validatePost(Post post) {

        try {
            StringBuilder totalComments = new StringBuilder();

            boolean isSafe = true;

            // Realistically, when user creates a VIDEO-type Post, it should only contain
            // 1 video. BUT if bug happens and they are able to create one with multiple videos, we still
            // are able to validate them, and should not throw an error. well mainly because mediaRepository
            // always return a list of media and not just one.

            if(post.getPostType().equals("IMAGE") || post.getPostType().equals("VIDEO")) {
                List<PostMedia> postMediaList = mediaRepository.findByPostId(post.getId());
                boolean isVideo = post.getPostType().equals("VIDEO");
                for(PostMedia postMedia : postMediaList) {
                    String ai_validation;
                    if(isVideo){
                        ai_validation = contentValidationService.validateVideoUrl(postMedia.getMediaUrl());
                    }else{
                        ai_validation = contentValidationService.validateImageUrl(postMedia.getMediaUrl());
                    }
                    String[] media_validation_split = ai_validation.split("@");
                    if(media_validation_split.length<2){
                        throw new RuntimeException("AI returned incorrect form");
                    }
                    postMedia.setAiValidationComment(media_validation_split[0]);
                    postMedia.setAiValidationStatus(media_validation_split[1]);
                    mediaRepository.save(postMedia);
                    if(media_validation_split[1].equals("AI_UNSAFE")) isSafe = false;
                }
            }

            if(!isSafe){
                totalComments.append("Some medias are found not safe.");
            }else totalComments.append("All medias are found safe.");


            String textValidation = contentValidationService.validateText(post.getContent());
            String[] text_validation_split = textValidation.split("@");
            if(text_validation_split.length<2){
                throw new RuntimeException("AI returned incorrect form");
            }
            if(text_validation_split[1].equals("AI_UNSAFE")){
                isSafe=false;
            }
            totalComments.append(text_validation_split[0]);
            post.setAiValidationComment(totalComments.toString());

            if(isSafe) post.setAiValidationStatus("AI_SAFE");
            else post.setAiValidationStatus("AI_UNSAFE");
            postRepository.save(post);

        } catch (Exception ermWhatTheSigma) {
            System.out.println("Error while validation post");
            ermWhatTheSigma.printStackTrace();
        }

    }
}
