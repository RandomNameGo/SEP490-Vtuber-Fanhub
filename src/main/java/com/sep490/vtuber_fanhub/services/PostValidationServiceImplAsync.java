package com.sep490.vtuber_fanhub.services;

import com.fasterxml.jackson.databind.JsonNode;
import com.sep490.vtuber_fanhub.models.Post;
import com.sep490.vtuber_fanhub.models.PostMedia;
import com.sep490.vtuber_fanhub.repositories.PostMediaRepository;
import com.sep490.vtuber_fanhub.repositories.PostRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.List;

@RequiredArgsConstructor
@Service("postValidationServiceImplAsync")
public class PostValidationServiceImplAsync implements PostValidationService {

    private final PostMediaRepository mediaRepository;
    private final ContentValidationService contentValidationService;
    private final PostRepository postRepository;
    private final PostMediaRepository postMediaRepository;
    private final String MEDIA_SAFE_COMMENT = " All medias are found safe.";
    private final String MEDIA_UNSAFE_COMMENT = " Some medias are found Unsafe.";

    @Override
    @Async("validationExecutor")
    public void validatePost(Post post) {
        try {
            // validate text first
            String textValidation = contentValidationService.validateText(post.getContent());
            String[] text_validation_split = textValidation.split("@");
            if(text_validation_split.length<2){
                throw new RuntimeException("AI returned incorrect form");
            }
            if(text_validation_split[1].equals("AI_UNSAFE")){
                post.setAiValidationStatus("AI_UNSAFE");
            }else if(text_validation_split[1].equals("AI_SAFE")){
                post.setAiValidationStatus("AI_SAFE");
            }
            else throw new RuntimeException("AI returned incorrect form");

            post.setAiValidationComment(textValidation);

            postRepository.save(post);

            // validate media
            // if media is image type, we can solve it synchronously
            if(post.getPostType().equals("IMAGE")) {
                List<PostMedia> postMediaList = mediaRepository.findByPostId(post.getId());
                for(PostMedia postMedia : postMediaList) {
                    String ai_validation = contentValidationService.validateImageUrl(postMedia.getMediaUrl());
                    String[] media_validation_split = ai_validation.split("@");
                    if(media_validation_split.length<2){
                        throw new RuntimeException("AI returned incorrect form");
                    }
                    postMedia.setAiValidationComment(media_validation_split[0]);
                    postMedia.setAiValidationStatus(media_validation_split[1]);
                    mediaRepository.save(postMedia);

                    finalizeValidation(post);
                }

            }else if(post.getPostType().equals("VIDEO")){
                // if a post is video type, there suppose to be only 1 video
                // but if somehow user sent multiple, we still are able to solve it
                List<PostMedia> postMediaList = mediaRepository.findByPostId(post.getId());
                for(PostMedia postMedia : postMediaList) {
                    // send to api, api will then call back to handleVideoCallback, where it will call finalize
                    JsonNode response = contentValidationService.validateVideoUrlAsync(postMedia.getMediaUrl());
                    String sight_engine_media_id = response.path("media").path("id").asText();
                    if(sight_engine_media_id.isEmpty()){
                        throw new RuntimeException("cannot find sight engine's media id");
                    }
                    postMedia.setAiValidationStatus("PROCESSING");
                    postMedia.setSightEngineMediaId(sight_engine_media_id);
                    mediaRepository.save(postMedia);
                }
            }

        } catch (Exception ermWhatTheSigma) {
            ermWhatTheSigma.printStackTrace();
        }
    }

    @Override
    @Async("validationExecutor")
    public void handleVideoCallback(JsonNode node){
        try{
            String mediaId = node.path("media").path("id").asText();
            PostMedia postMedia = postMediaRepository.findBySightEngineMediaId(mediaId)
                    .orElseThrow(() -> new RuntimeException("post media with sight engine media id not found"));
            String ai_validation = contentValidationService.handleCallbackResult(node.path("data"));
            String[] media_validation_split = ai_validation.split("@");
            if(media_validation_split.length<2){
                throw new RuntimeException("AI returned incorrect form");
            }
            postMedia.setAiValidationComment(media_validation_split[0]);
            postMedia.setAiValidationStatus(media_validation_split[1]);
            mediaRepository.save(postMedia);

            // Assuming all media validation is done, attempting to finalize post.
            Post post = postMedia.getPost();
            finalizeValidation(post);
        }catch(Exception ermWhatTheSigma){
            ermWhatTheSigma.printStackTrace();
        }

    }

    @Override
    public void finalizeValidation(Post post){
        try{
            List<PostMedia> postMediaList = postMediaRepository.findByPostId(post.getId());
            boolean mediaSafe = true;
            for(PostMedia postMedia : postMediaList) {
                if(postMedia.getAiValidationStatus().equals("PENDING") ||
                        postMedia.getAiValidationStatus().equals("PROCESSING")){
                    throw new RuntimeException("Some medias are not validated yet. finalize failed.");
                }
                if(postMedia.getAiValidationStatus().equals("AI_UNSAFE")){
                    mediaSafe = false;
                }
            }
            if(!mediaSafe){
                String oldAiComment = post.getAiValidationComment();
                post.setAiValidationStatus(oldAiComment + MEDIA_UNSAFE_COMMENT);
                post.setAiValidationStatus("AI_UNSAFE");
                postRepository.save(post);
            }else{
                String oldAiComment = post.getAiValidationComment();
                post.setAiValidationComment(oldAiComment + MEDIA_SAFE_COMMENT);
                postRepository.save(post);
            }
        }catch(Exception ermWhatTheSigma){
        }
    }
}
