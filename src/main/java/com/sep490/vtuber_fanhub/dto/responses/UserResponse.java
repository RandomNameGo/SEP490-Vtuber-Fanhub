package com.sep490.vtuber_fanhub.dto.responses;

import lombok.Data;

import java.time.Instant;
import java.util.List;

@Data
public class UserResponse {

    private Long userId;
    private String username;
    private String email;
    private String displayName;
    private String avatarUrl;
    private String frameUrl;
    private String bio;
    private String role;
    private Long points;
    private Long paidPoints;
    private String translateLanguage;
    private Instant createdAt;
    private Instant updatedAt;
    private Boolean isActive;

    private List<UserBadgeResponse> badges;

    @Data
    public static class UserBadgeResponse {
        private Long userBadgeId;
        private Long badgeId;
        private String badgeName;
        private String description;
        private String iconUrl;
        private String requirement;
        private Instant acquiredAt;
        private Boolean isDisplay;
    }
}
