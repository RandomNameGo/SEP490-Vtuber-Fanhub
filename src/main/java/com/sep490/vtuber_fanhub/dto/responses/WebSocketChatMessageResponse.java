package com.sep490.vtuber_fanhub.dto.responses;

import lombok.Builder;
import lombok.Data;

import java.time.Instant;

@Data
@Builder
public class WebSocketChatMessageResponse {
    private Long id;
    private String senderRole;
    private String content;
    private Instant createdAt;
    private Long sessionId;
}
