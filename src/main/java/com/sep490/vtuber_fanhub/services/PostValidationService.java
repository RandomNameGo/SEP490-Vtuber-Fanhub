package com.sep490.vtuber_fanhub.services;

import com.sep490.vtuber_fanhub.exceptions.NotFoundException;
import com.sep490.vtuber_fanhub.models.Post;
import com.sep490.vtuber_fanhub.models.PostMedia;
import com.sep490.vtuber_fanhub.repositories.PostMediaRepository;
import com.sep490.vtuber_fanhub.repositories.PostRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.sql.SQLOutput;
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

        try {
            // do something idk
        } catch (Exception ermWhatTheSigma) {
            ermWhatTheSigma.printStackTrace();
        }

    }

    @Async("validationExecutor")
    public void validatePost(Post post) {
        System.out.println("Validating post!");

        try {
            StringBuilder totalComments = new StringBuilder();

            boolean isSafe = true;
            if(post.getPostType().equals("IMAGE") || post.getPostType().equals("VIDEO")) {
                List<PostMedia> postMediaList = mediaRepository.findByPostId(post.getId());
                for(PostMedia postMedia : postMediaList) {
                    String ai_validation = contentValidationService.validateMediaUrl(postMedia.getMediaUrl());
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
