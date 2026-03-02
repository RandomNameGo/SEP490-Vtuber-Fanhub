package com.sep490.vtuber_fanhub.repositories;

import com.sep490.vtuber_fanhub.models.Post;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface PostRepository extends JpaRepository<Post, Long> {

    Page<Post> findByHubIdAndStatus(Long fanHubId, String status, Pageable pageable);

    @Query("SELECT DISTINCT p FROM Post p " +
            "LEFT JOIN PostHashtag ph ON p.id = ph.post.id " +
            "WHERE p.hub.id = :fanHubId " +
            "AND p.status = :status " +
            "AND (:hashtag IS NULL OR ph.hashtag = :hashtag)")
    Page<Post> findByHubIdAndStatusAndHashtag(
            @Param("fanHubId") Long fanHubId,
            @Param("status") String status,
            @Param("hashtag") String hashtag,
            Pageable pageable);
}