package com.sep490.vtuber_fanhub.dto.responses;

import lombok.Builder;
import lombok.Data;

import java.time.Instant;

/**
 * DTO for SSE notification events sent to clients
 * Used for real-time notifications when:
 * - VTuber application is approved/rejected
 * - Post receives a new like
 * - Post receives a new comment
 */
@Data
@Builder
public class NotificationEventResponse {
    
    /**
     * Unique notification ID
     */
    private Long id;
    
    /**
     * Type of notification event
     * Examples: VTUBER_APPLICATION_APPROVED, VTUBER_APPLICATION_REJECTED, 
     * POST_LIKE, POST_COMMENT
     */
    private String type;
    
    /**
     * Short title for the notification
     */
    private String title;
    
    /**
     * Detailed message content
     */
    private String message;
    
    /**
     * Related FanHub ID if applicable
     */
    private Long relatedHubId;
    
    /**
     * Related FanHub name if applicable
     */
    private String relatedHubName;
    
    /**
     * Related Post ID if applicable
     */
    private Long relatedPostId;
    
    /**
     * Post title if applicable
     */
    private String relatedPostTitle;
    
    /**
     * User who triggered the notification (e.g., who liked/commented)
     */
    private Long triggeredByUserId;
    
    /**
     * Username of user who triggered the notification
     */
    private String triggeredByUsername;
    
    /**
     * Avatar URL of user who triggered the notification
     */
    private String triggeredByAvatarUrl;
    
    /**
     * Notification creation timestamp
     */
    private Instant createdAt;
}
