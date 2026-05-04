package com.sep490.vtuber_fanhub.dto.responses;

import lombok.Builder;
import lombok.Data;

import java.time.Instant;
import java.util.Map;

@Data
@Builder
public class FanHubModelResponse {
    private Long id;
    private String name;
    private String fileUrl;
    private Map<String, Object> sprites;
    private Instant createdAt;
}
