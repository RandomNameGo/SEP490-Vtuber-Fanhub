package com.sep490.vtuber_fanhub.repositories;

import com.sep490.vtuber_fanhub.models.Post;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface PostRepository extends JpaRepository<Post, Long> {

    Page<Post> findByHubIdAndStatus(Long fanHubId, String status, Pageable pageable);

    @Query("select distinct p from Post p " +
            "left join PostHashtag ph on p.id = ph.post.id " +
            "where p.hub.id = :fanHubId " +
            "and p.status = :status " +
            "and (:hashtag is null or ph.hashtag = :hashtag)")
    Page<Post> findByHubIdAndStatusAndHashtag(
            Long fanHubId,
            String status,
            String hashtag,
            Pageable pageable);

    @Query("select p from Post p " +
            "where p.hub.id = :fanHubId " +
            "and p.status = :status " +
            "and p.postType in :postTypes")
    Page<Post> findByHubIdAndStatusAndPostTypes(
            Long fanHubId,
            String status,
            List<String> postTypes,
            Pageable pageable);

    //Find posts from specific hub IDs (user's followed hubs)
    @Query("select p from Post p " +
            "where p.hub.id in :hubIds " +
            "and p.status = 'APPROVED' " +
            "order by p.createdAt desc")
    Page<Post> findByHubIdInAndStatusApproved(List<Long> hubIds, Pageable pageable);

    //Find public posts with similar categories for suggestions
    @Query("select distinct p from Post p " +
            "join FanHubCategory fc on p.hub.id = fc.hub.id " +
            "where p.hub.isPrivate = false " +
            "and p.hub.id not in :excludedHubIds " +
            "and p.status = 'APPROVED' " +
            "and fc.categoryName in :categories " +
            "order by p.createdAt desc")
    Page<Post> findPublicPostsByCategories(
            List<Long> excludedHubIds,
            List<String> categories,
            Pageable pageable);

    //Find any public posts (fallback for suggestions)
    @Query("select p from Post p " +
            "where p.hub.isPrivate = false " +
            "and p.hub.id not in :excludedHubIds " +
            "and p.status = 'APPROVED' " +
            "order by p.createdAt desc")
    Page<Post> findPublicPosts(
            List<Long> excludedHubIds,
            Pageable pageable);

    //Find public posts sorted by interaction count
    @Query("select p from Post p " +
            "left join PostLike pl on p.id = pl.post.id " +
            "left join PostComment pc on p.id = pc.post.id " +
            "where p.hub.isPrivate = false " +
            "and p.status = 'APPROVED' " +
            "group by p.id " +
            "order by count(distinct pl.id) + count(distinct pc.id) desc, p.createdAt desc")
    Page<Post> findPublicPostsOrderByInteractions(Pageable pageable);


     //Get categories from user's followed hubs
    @Query("select distinct fc.categoryName from FanHubCategory fc " +
            "where fc.hub.id in :hubIds")
    List<String> findCategoriesByHubIds(List<Long> hubIds);

    //Find posts by username with pagination
    @Query("select p from Post p " +
            "where p.user.username = :username " +
            "order by p.createdAt desc")
    Page<Post> findByUsername(String username, Pageable pageable);
}