package com.sep490.vtuber_fanhub.controllers;

import com.sep490.vtuber_fanhub.dto.responses.APIResponse;
import com.sep490.vtuber_fanhub.models.Notification;
import com.sep490.vtuber_fanhub.services.NotificationService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

/**
 * REST Controller for Notification management
 * Provides endpoints for users to manage their notifications
 * 
 * Endpoints:
 * - GET /notifications - Get all notifications for current user
 * - GET /notifications/unread - Get unread notifications
 * - GET /notifications/unread/count - Get count of unread notifications
 * - POST /notifications/{id}/read - Mark notification as read
 * - POST /notifications/read-all - Mark all notifications as read
 * - DELETE /notifications/{id} - Delete a notification
 * - DELETE /notifications/all - Delete all notifications
 */
@RestController
@RequestMapping("vhub/api/v1/notifications")
@RequiredArgsConstructor
@Slf4j
public class NotificationController {

    private final NotificationService notificationService;

    /**
     * Get all notifications for the current user with pagination
     *
     * @param request the HTTP request containing the JWT token
     * @param pageNo page number (default: 0)
     * @param pageSize page size (default: 20)
     * @param sortBy sort field (default: createdAt)
     * @return list of notifications
     */
    @GetMapping
    public ResponseEntity<?> getNotifications(
            HttpServletRequest request,
            @RequestParam(defaultValue = "0") int pageNo,
            @RequestParam(defaultValue = "20") int pageSize,
            @RequestParam(defaultValue = "createdAt") String sortBy) {

        List<Notification> notifications = notificationService.getUserNotifications(
                request, pageNo, pageSize, sortBy);

        return ResponseEntity.ok()
                .body(APIResponse.<List<Notification>>builder()
                        .success(true)
                        .message("Notifications retrieved successfully")
                        .data(notifications)
                        .build());
    }

    /**
     * Get unread notifications for the current user
     *
     * @param request the HTTP request containing the JWT token
     * @param pageNo page number (default: 0)
     * @param pageSize page size (default: 20)
     * @param sortBy sort field (default: createdAt)
     * @return list of unread notifications
     */
    @GetMapping("/unread")
    public ResponseEntity<?> getUnreadNotifications(
            HttpServletRequest request,
            @RequestParam(defaultValue = "0") int pageNo,
            @RequestParam(defaultValue = "20") int pageSize,
            @RequestParam(defaultValue = "createdAt") String sortBy) {

        List<Notification> notifications = notificationService.getUnreadNotifications(
                request, pageNo, pageSize, sortBy);

        return ResponseEntity.ok()
                .body(APIResponse.<List<Notification>>builder()
                        .success(true)
                        .message("Unread notifications retrieved successfully")
                        .data(notifications)
                        .build());
    }

    /**
     * Get count of unread notifications for the current user
     * Useful for displaying notification badge count
     *
     * @param request the HTTP request containing the JWT token
     * @return count of unread notifications
     */
    @GetMapping("/unread/count")
    public ResponseEntity<?> getUnreadNotificationCount(HttpServletRequest request) {

        Long count = notificationService.getUnreadNotificationCount(request);

        return ResponseEntity.ok()
                .body(APIResponse.<Long>builder()
                        .success(true)
                        .message("Unread notification count retrieved successfully")
                        .data(count)
                        .build());
    }

    /**
     * Mark a specific notification as read
     *
     * @param request the HTTP request containing the JWT token
     * @param notificationId the notification ID to mark as read
     * @return success response
     */
    @PostMapping("/{notificationId}/read")
    public ResponseEntity<?> markAsRead(
            HttpServletRequest request,
            @PathVariable Long notificationId) {

        String result = notificationService.markAsRead(notificationId, request);

        return ResponseEntity.ok()
                .body(APIResponse.<String>builder()
                        .success(true)
                        .message(result)
                        .build());
    }

    /**
     * Mark all notifications as read for the current user
     *
     * @param request the HTTP request containing the JWT token
     * @return number of notifications marked as read
     */
    @PostMapping("/read-all")
    public ResponseEntity<?> markAllAsRead(HttpServletRequest request) {

        int count = notificationService.markAllAsRead(request);

        return ResponseEntity.ok()
                .body(APIResponse.<Integer>builder()
                        .success(true)
                        .message("All notifications marked as read")
                        .data(count)
                        .build());
    }

    /**
     * Delete a specific notification
     *
     * @param request the HTTP request containing the JWT token
     * @param notificationId the notification ID to delete
     * @return success response
     */
    @DeleteMapping("/{notificationId}")
    public ResponseEntity<?> deleteNotification(
            HttpServletRequest request,
            @PathVariable Long notificationId) {

        String result = notificationService.deleteNotification(notificationId, request);

        return ResponseEntity.ok()
                .body(APIResponse.<String>builder()
                        .success(true)
                        .message(result)
                        .build());
    }

    /**
     * Delete all notifications for the current user
     *
     * @param request the HTTP request containing the JWT token
     * @return success response
     */
    @DeleteMapping("/all")
    public ResponseEntity<?> deleteAllNotifications(HttpServletRequest request) {

        String result = notificationService.deleteAllNotifications(request);

        return ResponseEntity.ok()
                .body(APIResponse.<String>builder()
                        .success(true)
                        .message(result)
                        .build());
    }
}
