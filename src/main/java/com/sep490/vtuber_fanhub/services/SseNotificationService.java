package com.sep490.vtuber_fanhub.services;

import com.sep490.vtuber_fanhub.dto.responses.NotificationEventResponse;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

/**
 * Service interface for managing SSE (Server-Sent Events) connections
 * and sending real-time notifications to users
 */
public interface SseNotificationService {

    /**
     * Create a new SSE emitter for a user and store the connection
     *
     * @param userId the user ID to create emitter for
     * @return the created SseEmitter
     */
    SseEmitter createEmitter(Long userId);

    /**
     * Remove an SSE emitter for a user (called on disconnect/timeout)
     *
     * @param userId the user ID to remove emitter for
     */
    void removeEmitter(Long userId);

    /**
     * Send a notification to a specific user via SSE
     *
     * @param userId the user ID to send notification to
     * @param event the notification event to send
     */
    void sendNotification(Long userId, NotificationEventResponse event);

    /**
     * Send VTuber application approval/rejection notification
     *
     * @param userId the user ID to notify
     * @param status the application status (ACCEPTED/REJECTED)
     * @param reason the review reason
     */
    void sendVtuberApplicationNotification(Long userId, String status, String reason);

    /**
     * Send post like notification to post author
     *
     * @param postAuthorId the post author's user ID
     * @param likedByUserId the user who liked the post
     * @param likedByUsername the username who liked the post
     * @param likedByAvatarUrl the avatar URL of user who liked
     * @param postId the post ID that was liked
     * @param postTitle the post title
     * @param fanHubId the FanHub ID
     * @param fanHubName the FanHub name
     */
    void sendPostLikeNotification(Long postAuthorId, Long likedByUserId, String likedByUsername,
                                   String likedByAvatarUrl, Long postId, String postTitle,
                                   Long fanHubId, String fanHubName);

    /**
     * Send post comment notification to post author
     *
     * @param postAuthorId the post author's user ID
     * @param commentedByUserId the user who commented
     * @param commentedByUsername the username who commented
     * @param commentedByAvatarUrl the avatar URL of user who commented
     * @param postId the post ID that was commented on
     * @param postTitle the post title
     * @param fanHubId the FanHub ID
     * @param fanHubName the FanHub name
     */
    void sendPostCommentNotification(Long postAuthorId, Long commentedByUserId, String commentedByUsername,
                                      String commentedByAvatarUrl, Long postId, String postTitle,
                                      Long fanHubId, String fanHubName);

    /**
     * Get the count of active SSE emitters (for monitoring/debugging)
     *
     * @return the number of active connections
     */
    int getActiveEmitterCount();
}
