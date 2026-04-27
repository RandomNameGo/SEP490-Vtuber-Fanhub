package com.sep490.vtuber_fanhub.repositories;

import com.sep490.vtuber_fanhub.models.Notification;
import com.sep490.vtuber_fanhub.models.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/**
 * Repository for Notification entity
 * Provides database operations for user notifications
 */
@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {


    @EntityGraph(attributePaths = {"user", "relatedHub", "relatedPost", "triggeredBy"})
    Page<Notification> findByUser(User user, Pageable pageable);

    @EntityGraph(attributePaths = {"user", "relatedHub", "relatedPost", "triggeredBy"})
    Page<Notification> findByUserAndIsReadFalse(User user, Pageable pageable);


    Long countByUserAndIsReadFalse(User user);


    @EntityGraph(attributePaths = {"user", "relatedHub", "relatedPost", "triggeredBy"})
    Page<Notification> findByUserAndType(User user, String type, Pageable pageable);

    void deleteByUser(User user);

    @Modifying
    @Query("UPDATE Notification n SET n.isRead = true WHERE n.user.id = :userId AND n.isRead = false")
    int markAllAsRead(@Param("userId") Long userId);


    @Query("SELECT MAX(n.id) FROM Notification n")
    Long findMaxId();
}
