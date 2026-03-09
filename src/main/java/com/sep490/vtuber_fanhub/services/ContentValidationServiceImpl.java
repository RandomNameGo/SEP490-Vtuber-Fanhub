package com.sep490.vtuber_fanhub.services;

import com.fasterxml.jackson.databind.JsonNode;
import com.sep490.vtuber_fanhub.models.Enum.PostMediaType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
public class ContentValidationServiceImpl implements ContentValidationService{

    private final GroqAIService groqAIService;
    private final SightEngineService sightEngineService;

    @Override
    public String validateText(String text) {
        String intentPrompt = String.format("""
            Your task is to validate the following text from user
            Text must not contain any inappropriate languages, hate, or discrimination
            But at the same time, dont be too strict.
            
            The respond Must Not be in quotes. Only text.
            Keep your answer as short as possible, while maintaining all key points.
            !!!IMPORTANT: Your answer must be in format: "comment@status"
            !!!IMPORTANT: status must be either "AI_SAFE" or "AI_UNSAFE"
            
            Example: This commment is safe@AI_SAFE
            Example: this comment is not safe. bad keywords found are: abc.@AI_UNSAFE
            
            Text: "%s"
            
            """, text);

        return groqAIService.sendPrompt(intentPrompt).trim();
    }

    @Override
    public String validateImageFile(MultipartFile file) {
        JsonNode mediaValidationResult = sightEngineService.checkMediaFile(file, PostMediaType.IMAGE);
        String intentPrompt = String.format("""
            Your task is to provide your comment based on the following result of SightEngine
            
            
            The respond Must Not be in quotes. Only text.
            Keep your answer as short as possible, while maintaining all key points.
            !!!IMPORTANT:Your answer must be in format: "comment@status"
            !!!IMPORTANT: status must be either "AI_SAFE" or "AI_UNSAFE"
            
            Example: This image is safe@AI_SAFE
            Example: this image is not safe. based on data received, this image is gore-y.@AI_UNSAFE
            
            Text: "%s"
            
            """, mediaValidationResult.toString());
        return groqAIService.sendPrompt(intentPrompt).trim();
    }

    @Override
    public String validateImageUrl(String url) {
        JsonNode mediaValidationResult = sightEngineService.checkMediaUrl(url, PostMediaType.IMAGE);
        String intentPrompt = String.format("""
            Your task is to provide your comment based on the following result of SightEngine
            
            
            The respond Must Not be in quotes. Only text.
            Keep your answer as short as possible, while maintaining all key points.
            !!!IMPORTANT: Your answer must be in format: "comment@status"
            !!!IMPORTANT: status must be either "AI_SAFE" or "AI_UNSAFE"
            
            Example: This image is safe@AI_SAFE
            Example: this image is not safe. based on data received, this image is gore-y.@AI_UNSAFE
            
            SightEngine Result: "%s"
            
            """, mediaValidationResult.toString());
        return groqAIService.sendPrompt(intentPrompt).trim();
    }

    @Override
    public String validateVideoUrl(String url) {
        JsonNode mediaValidationResult = sightEngineService.checkMediaUrl(url, PostMediaType.VIDEO);
        String intentPrompt = String.format("""
            Your task is to provide your comment based on the following result of SightEngine
            
            
            The respond Must Not be in quotes. Only text.
            Keep your answer as short as possible, while maintaining all key points.
            !!!IMPORTANT: Your answer must be in format: "comment@status"
            !!!IMPORTANT: status must be either "AI_SAFE" or "AI_UNSAFE"
            
            Example: This video is safe@AI_SAFE
            Example: this video is not safe. based on data received, this image is gore-y.@AI_UNSAFE
            
            SightEngine Result: "%s"
            
            """, mediaValidationResult.toString());
        return groqAIService.sendPrompt(intentPrompt).trim();
    }
}
