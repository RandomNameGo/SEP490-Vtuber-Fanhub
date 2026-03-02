package com.sep490.vtuber_fanhub.dto.responses;

import lombok.Data;

import java.time.Instant;
import java.util.List;

@Data
public class PostResponse {

    private Long postId;
    private Long fanHubId;
    private String fanHubName;

    private Long authorId;
    private String authorUsername;
    private String authorDisplayName;
    private String authorAvatarUrl;

    private String postType;
    private String title;
    private String content;
    private String status;
    private Boolean isPinned;

    private List<String> mediaUrls;
    private List<String> hashtags;

    private Instant createdAt;
    private Instant updatedAt;
}
