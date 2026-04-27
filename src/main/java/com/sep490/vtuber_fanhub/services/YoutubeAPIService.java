package com.sep490.vtuber_fanhub.services;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.concurrent.CompletableFuture;

@Service
@RequiredArgsConstructor
@Slf4j
public class YoutubeAPIService {

    private final RestTemplate restTemplate;

    @Value("${youtube.api_key}")
    private String apiKey;

    public String testGetChannel(){
        String requestUrl = UriComponentsBuilder.fromHttpUrl("https://www.googleapis.com/youtube/v3/channels")
                .queryParam("part", "id")
                .queryParam("forHandle", "@f1watafak")
                .queryParam("key", apiKey)
                .toUriString();

        try {
            // Gọi API và lấy response dạng JsonNode
            JsonNode response = restTemplate.getForObject(requestUrl, JsonNode.class);

            // Parse JSON để lấy channel ID
            if (response != null && response.has("items") && response.get("items").size() > 0) {
                String channelId = response.get("items").get(0).get("id").asText();

                // Log ID cụ thể đã bóc tách được
                log.info("Successfully fetched Channel ID: {} for handle: {}", channelId);
                return channelId;
            }
        } catch (Exception e) {
            // Xử lý lỗi (ví dụ: API Key sai, Handle không tồn tại, v.v.)
            System.err.println("Lỗi khi gọi YouTube API: " + e.getMessage());
        }

        return null;
    }

    @Async
    public CompletableFuture<String> getChannelIdByUrl(String url){

        String[] parts = url.split("/@");
        String handle = "@" + parts[1].split("/")[0];

        String requestUrl = UriComponentsBuilder.fromHttpUrl("https://www.googleapis.com/youtube/v3/channels")
                .queryParam("part", "id")
                .queryParam("forHandle", handle)
                .queryParam("key", apiKey)
                .toUriString();

        try {
            JsonNode response = restTemplate.getForObject(requestUrl, JsonNode.class);

            if (response != null && response.has("items") && response.get("items").size() > 0) {

                return CompletableFuture.completedFuture(response.get("items").get(0).get("id").asText());
            }
        } catch (Exception e) {
            System.err.println("Error" + e.getMessage());
        }

        return CompletableFuture.completedFuture(null);
    }
}
