package com.sep490.vtuber_fanhub.services;

import com.fasterxml.jackson.databind.JsonNode;
import com.sep490.vtuber_fanhub.models.Enum.PostMediaType;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@Service
public class SightEngineServiceImpl implements SightEngineService{


    @Value("${sightengine.workflow.images}")
    private String imageWorkflowId;

    @Value("${sightengine.workflow.videos}")
    private String videoWorkflowId;

    @Value("${sightengine.user}")
    private String apiUser;

    @Value("${sightengine.secret}")
    private String apiSecret;

    @Override
    public JsonNode checkMediaFile(MultipartFile file, PostMediaType mediaType) {
        String apiUrl;
        if(mediaType.equals(PostMediaType.IMAGE)){
            apiUrl = "https://api.sightengine.com/1.0/check-workflow.json";
        }
        else if(mediaType.equals(PostMediaType.VIDEO)){
            apiUrl = "https://api.sightengine.com/1.0/video/check-workflow-sync.json";
        }else throw new RuntimeException("Invalid Media Type");
        RestTemplate restTemplate = new RestTemplate();
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.MULTIPART_FORM_DATA);

            MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
            if(mediaType.equals(PostMediaType.IMAGE)){
                body.add("workflow", imageWorkflowId);
            }
            else if(mediaType.equals(PostMediaType.VIDEO)){
                body.add("workflow", videoWorkflowId);
            }
            body.add("api_user", apiUser);
            body.add("api_secret", apiSecret);

            body.add("media", new ByteArrayResource(file.getBytes()) {
                @Override
                public String getFilename() {
                    return file.getOriginalFilename();
                }
            });

            HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);
            ResponseEntity<JsonNode> response = restTemplate.postForEntity(apiUrl, requestEntity, JsonNode.class);
            return response.getBody();

        } catch (IOException e) {
            throw new RuntimeException("Error processing image upload", e);
        }
    }

    @Override
    public JsonNode checkMediaUrl(String url, PostMediaType mediaType) {
        String apiUrl;
        if(mediaType.equals(PostMediaType.IMAGE)){
            apiUrl = "https://api.sightengine.com/1.0/check-workflow.json";
        }
        else if(mediaType.equals(PostMediaType.VIDEO)){
            apiUrl = "https://api.sightengine.com/1.0/video/check-workflow-sync.json";
        }else throw new RuntimeException("Invalid Media Type");

        RestTemplate restTemplate = new RestTemplate();
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.MULTIPART_FORM_DATA);

            MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();

            if(mediaType.equals(PostMediaType.IMAGE)){
                body.add("workflow", imageWorkflowId);
            }
            else body.add("workflow", videoWorkflowId);

            body.add("api_user", apiUser);
            body.add("api_secret", apiSecret);

            if(mediaType.equals(PostMediaType.IMAGE)){
                body.add("url", url);
            }
            else body.add("stream_url", url);


            HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);
            ResponseEntity<JsonNode> response = restTemplate.postForEntity(apiUrl, requestEntity, JsonNode.class);
            return response.getBody();

        } catch (Exception e) {
            e.printStackTrace();
            System.out.println(e.getMessage());
            throw new RuntimeException("Error processing image url upload", e);
        }
    }


}
