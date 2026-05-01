package com.sep490.vtuber_fanhub.repositories;

import com.sep490.vtuber_fanhub.models.Post;
import com.sep490.vtuber_fanhub.models.PostComment;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface PostCommentRepository extends JpaRepository<PostComment, Long> {

    @Modifying
    @Query("UPDATE PostComment pc SET pc.memberId = NULL WHERE pc.memberId = :memberId")
    void nullifyMemberId(@Param("memberId") Long memberId);

    @Modifying
    @Query("UPDATE PostComment pc SET pc.memberId = :memberId " +
            "WHERE pc.user.id = :userId AND pc.post.id IN (SELECT p.id FROM Post p WHERE p.hub.id = :hubId)")
    void updateMemberIdByUserIdAndHubId(@Param("userId") Long userId, @Param("hubId") Long hubId, @Param("memberId") Long memberId);

    @EntityGraph(attributePaths = {"user"})
    List<PostComment> findByPostIdOrderByCreatedAtAsc(Long postId);

    @EntityGraph(attributePaths = {"user"})
    List<PostComment> findByPostOrderByCreatedAtAsc(Post post);

    @EntityGraph(attributePaths = {"user"})
    List<PostComment> findByPostIdAndParentCommentIsNullOrderByCreatedAtAsc(Long postId);

    Long countByPostId(Long postId);

    @EntityGraph(attributePaths = {"user"})
    List<PostComment> findByParentCommentIdOrderByCreatedAtAsc(Long parentCommentId);

    boolean existsByParentCommentId(Long parentCommentId);
}