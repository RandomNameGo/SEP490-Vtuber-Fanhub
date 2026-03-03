package com.sep490.vtuber_fanhub.repositories;

import com.sep490.vtuber_fanhub.models.Post;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

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

    /**
     * Find posts from specific hub IDs (user's followed hubs)
     */
    @Query("SELECT p FROM Post p " +
            "WHERE p.hub.id IN :hubIds " +
            "AND p.status = 'APPROVED' " +
            "ORDER BY p.createdAt DESC")
    Page<Post> findByHubIdInAndStatusApproved(@Param("hubIds") List<Long> hubIds, Pageable pageable);

    //Find public posts with similar categories for suggestions
    @Query("SELECT DISTINCT p FROM Post p " +
            "JOIN FanHubCategory fc ON p.hub.id = fc.hub.id " +
            "WHERE p.hub.isPrivate = false " +
            "AND p.hub.id NOT IN :excludedHubIds " +
            "AND p.status = 'APPROVED' " +
            "AND fc.categoryName IN :categories " +
            "ORDER BY p.createdAt DESC")
    Page<Post> findPublicPostsByCategories(
            @Param("excludedHubIds") List<Long> excludedHubIds,
            @Param("categories") List<String> categories,
            Pageable pageable);

    //Find any public posts (fallback for suggestions)
    @Query("SELECT p FROM Post p " +
            "WHERE p.hub.isPrivate = false " +
            "AND p.hub.id NOT IN :excludedHubIds " +
            "AND p.status = 'APPROVED' " +
            "ORDER BY p.createdAt DESC")
    Page<Post> findPublicPosts(
            @Param("excludedHubIds") List<Long> excludedHubIds,
            Pageable pageable);

    //Find public posts sorted by interaction count (likes + comments)
    @Query("SELECT p FROM Post p " +
            "LEFT JOIN PostLike pl ON p.id = pl.post.id " +
            "LEFT JOIN PostComment pc ON p.id = pc.post.id " +
            "WHERE p.hub.isPrivate = false " +
            "AND p.status = 'APPROVED' " +
            "GROUP BY p.id " +
            "ORDER BY COUNT(DISTINCT pl.id) + COUNT(DISTINCT pc.id) DESC, p.createdAt DESC")
    Page<Post> findPublicPostsOrderByInteractions(Pageable pageable);


     //Get categories from user's followed hubs
    @Query("SELECT DISTINCT fc.categoryName FROM FanHubCategory fc " +
            "WHERE fc.hub.id IN :hubIds")
    List<String> findCategoriesByHubIds(@Param("hubIds") List<Long> hubIds);
}