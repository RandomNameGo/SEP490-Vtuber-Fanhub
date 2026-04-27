package com.sep490.vtuber_fanhub.repositories;

import com.sep490.vtuber_fanhub.models.Post;
import com.sep490.vtuber_fanhub.models.PostComment;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PostCommentRepository extends JpaRepository<PostComment, Long> {

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