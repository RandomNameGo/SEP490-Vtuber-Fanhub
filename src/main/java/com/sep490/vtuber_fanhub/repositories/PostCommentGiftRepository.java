package com.sep490.vtuber_fanhub.repositories;

import com.sep490.vtuber_fanhub.models.PostComment;
import com.sep490.vtuber_fanhub.models.PostCommentGift;
import com.sep490.vtuber_fanhub.models.User;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PostCommentGiftRepository extends JpaRepository<PostCommentGift, Long> {

    @EntityGraph(attributePaths = {"sender", "receiver"})
    List<PostCommentGift> findByComment(PostComment comment);

    @EntityGraph(attributePaths = {"sender", "comment", "receiver"})
    Optional<PostCommentGift> findBySenderAndComment(User sender, PostComment comment);

    long countByReceiverId(Long receiverId);
}
