package com.sep490.vtuber_fanhub.services;

import com.fasterxml.jackson.databind.JsonNode;
import com.sep490.vtuber_fanhub.models.Post;
import com.sep490.vtuber_fanhub.models.PostMedia;
import com.sep490.vtuber_fanhub.repositories.PostMediaRepository;
import com.sep490.vtuber_fanhub.repositories.PostRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
    @Transactional
    public void validatePost(Post post) {
        try {
            System.out.println("Post validation async fired");

            // Set initial status to PENDING
            post.setFinalAiValidationStatus("PENDING");
            post.setContentAiValidationStatus("PENDING");
            postRepository.save(post);

            String textValidation = contentValidationService.validatePostContent(post.getTitle(),post.getContent());
            String[] text_validation_split = textValidation.split("@");
            if(text_validation_split.length<2){
                throw new RuntimeException("AI returned incorrect form");
            }

            post.setContentAiValidationStatus(text_validation_split[1]);
            post.setAiValidationComment(text_validation_split[0]);

            postRepository.save(post);

            // validate media
            // if media is image type, we can solve it synchronously
            if("IMAGE".equals(post.getPostType())) {
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
                }
                finalizeValidation(post);

            }else if("VIDEO".equals(post.getPostType())){
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
                    postMedia.setAiValidationStatus("PENDING");
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
    @Transactional
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
    @Transactional
    public void finalizeValidation(Post post){
        try{
            System.out.println("Finalizing validation for post: " + post.getId());

            // Check content validation status
            String contentStatus = post.getContentAiValidationStatus();
            if (contentStatus == null || "PENDING".equalsIgnoreCase(contentStatus)) {
                System.out.println("Content validation not finished yet for post " + post.getId());
                return;
            }

            List<PostMedia> postMediaList = postMediaRepository.findByPostId(post.getId());
            boolean allMediaProcessed = true;
            boolean anyMediaUnsafe = false;

            for(PostMedia postMedia : postMediaList) {
                String status = postMedia.getAiValidationStatus();
                if(status == null || "Pending".equalsIgnoreCase(status)){
                    allMediaProcessed = false;
                    break;
                }
                if("AI_UNSAFE".equals(status)){
                    anyMediaUnsafe = true;
                }
            }

            if(!allMediaProcessed){
                System.out.println("Some medias are not validated yet for post " + post.getId() + ". Finalize deferred.");
                return;
            }

            boolean isContentUnsafe = "AI_UNSAFE".equals(contentStatus);
            String currentComment = post.getAiValidationComment() != null ? post.getAiValidationComment() : "";

            if(isContentUnsafe || anyMediaUnsafe){
                post.setFinalAiValidationStatus("AI_UNSAFE");
                if (anyMediaUnsafe && !currentComment.contains(MEDIA_UNSAFE_COMMENT)) {
                    post.setAiValidationComment(currentComment + MEDIA_UNSAFE_COMMENT);
                }
            }else{
                post.setFinalAiValidationStatus("AI_SAFE");
                if (!currentComment.contains(MEDIA_SAFE_COMMENT)) {
                    post.setAiValidationComment(currentComment + MEDIA_SAFE_COMMENT);
                }
            }
            postRepository.save(post);
            System.out.println("Post " + post.getId() + " finalized with status: " + post.getFinalAiValidationStatus());
        }catch(Exception ermWhatTheSigma){
            ermWhatTheSigma.printStackTrace();
        }
    }
}
