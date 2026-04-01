package com.sep490.vtuber_fanhub.services;

import com.sep490.vtuber_fanhub.models.FanHub;
import com.sep490.vtuber_fanhub.models.Notification;
import com.sep490.vtuber_fanhub.models.Post;
import com.sep490.vtuber_fanhub.models.User;
import jakarta.servlet.http.HttpServletRequest;
import java.util.List;

/**
 * Service interface for Notification management
 * Handles both database persistence and SSE delivery of notifications
 */
public interface NotificationService {

    /**
     * Get all notifications for a user with pagination
     *
     * @param request the HTTP request containing the JWT token
     * @param pageNo page number
     * @param pageSize page size
     * @param sortBy sort field
     * @return list of notifications
     */
    List<Notification> getUserNotifications(HttpServletRequest request, int pageNo, int pageSize, String sortBy);

    /**
     * Get unread notifications for a user
     *
     * @param request the HTTP request containing the JWT token
     * @param pageNo page number
     * @param pageSize page size
     * @param sortBy sort field
     * @return list of unread notifications
     */
    List<Notification> getUnreadNotifications(HttpServletRequest request, int pageNo, int pageSize, String sortBy);

    /**
     * Get count of unread notifications for a user
     *
     * @param request the HTTP request containing the JWT token
     * @return count of unread notifications
     */
    Long getUnreadNotificationCount(HttpServletRequest request);

    /**
     * Mark a specific notification as read
     *
     * @param notificationId the notification ID
     * @param request the HTTP request containing the JWT token (for authorization)
     * @return success message
     */
    String markAsRead(Long notificationId, HttpServletRequest request);

    /**
     * Mark all notifications as read for a user
     *
     * @param request the HTTP request containing the JWT token
     * @return number of notifications marked as read
     */
    int markAllAsRead(HttpServletRequest request);

    /**
     * Delete a notification
     *
     * @param notificationId the notification ID
     * @param request the HTTP request containing the JWT token (for authorization)
     * @return success message
     */
    String deleteNotification(Long notificationId, HttpServletRequest request);

    /**
     * Delete all notifications for a user
     *
     * @param request the HTTP request containing the JWT token
     * @return success message
     */
    String deleteAllNotifications(HttpServletRequest request);

    /**
     * Create and persist a notification in database
     * Also sends it via SSE if user has active connection
     *
     * @param user the recipient user
     * @param type notification type
     * @param title notification title
     * @param message notification message
     * @param relatedHub related FanHub (optional)
     * @param relatedPost related Post (optional)
     * @param triggeredBy user who triggered the notification (optional)
     * @return the created notification
     */
    Notification createNotification(User user, String type, String title, String message,
                                     FanHub relatedHub, Post relatedPost, User triggeredBy);

    /**
     * Send VTuber application notification and persist to database
     *
     * @param userId the user ID to notify
     * @param status the application status (ACCEPTED/REJECTED)
     * @param reason the review reason
     */
    void sendVtuberApplicationNotification(Long userId, String status, String reason);

    /**
     * Send post like notification and persist to database
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
     * Send post comment notification and persist to database
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
}
