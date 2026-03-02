package com.sep490.vtuber_fanhub.services;

import com.fasterxml.jackson.databind.ObjectMapper;
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


    @Value("${sightengine.workflow}")
    private String workflowId;

    @Value("${sightengine.user}")
    private String apiUser;

    @Value("${sightengine.secret}")
    private String apiSecret;

    @Override
    public String checkImage(MultipartFile file) {
        String apiUrl = "https://api.sightengine.com/1.0/check-workflow.json";

        RestTemplate restTemplate = new RestTemplate();
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.MULTIPART_FORM_DATA);

            MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
            body.add("workflow", workflowId);
            body.add("api_user", apiUser);
            body.add("api_secret", apiSecret);

            body.add("media", new ByteArrayResource(file.getBytes()) {
                @Override
                public String getFilename() {
                    return file.getOriginalFilename();
                }
            });

            HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);
            ResponseEntity<String> response = restTemplate.postForEntity(apiUrl, requestEntity, String.class);
            return response.getBody();

        } catch (IOException e) {
            throw new RuntimeException("Error processing image upload", e);
        }
    }

}
