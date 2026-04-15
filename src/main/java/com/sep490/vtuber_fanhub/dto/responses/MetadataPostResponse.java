package com.sep490.vtuber_fanhub.dto.responses;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class MetadataPostResponse {
    private Long postId;
    private Long fanHubId;
    private String fanHubName;
    private String fanHubSubdomain;

    private Long authorId;
    private String authorUsername;
    private String authorDisplayName;
    private String authorAvatarUrl;

    private String postType;
    private String title;
    private String content;
    private String status;
}
