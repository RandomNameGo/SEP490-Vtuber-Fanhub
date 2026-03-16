package com.sep490.vtuber_fanhub.repositories;

import com.sep490.vtuber_fanhub.models.PostComment;
import com.sep490.vtuber_fanhub.models.PostCommentGift;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PostCommentGiftRepository extends JpaRepository<PostCommentGift, Long> {

    List<PostCommentGift> findByComment(PostComment comment);
}
