package com.sep490.vtuber_fanhub.dto.responses;

import lombok.Data;

import java.time.Instant;

@Data
public class PostCommentResponse {

    private Long commentId;
    private Long postId;

    private Long userId;
    private String username;
    private String displayName;
    private String avatarUrl;
    private String frameUrl;
    private java.math.BigDecimal frameSize;
    private java.math.BigDecimal frameXAxis;
    private java.math.BigDecimal frameYAxis;
    private Long memberId;

    private String content;
    private String status;

    private Long parentCommentId;

    private Instant createdAt;

    private Long likeCount;
    private Boolean isLikedByCurrentUser;
    private Boolean hasChildren;

    private Long giftCount;
}
