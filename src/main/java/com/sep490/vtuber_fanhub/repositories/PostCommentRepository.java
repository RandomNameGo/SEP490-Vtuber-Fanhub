package com.sep490.vtuber_fanhub.repositories;

import com.sep490.vtuber_fanhub.models.PostComment;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PostCommentRepository extends JpaRepository<PostComment, Long> {
}