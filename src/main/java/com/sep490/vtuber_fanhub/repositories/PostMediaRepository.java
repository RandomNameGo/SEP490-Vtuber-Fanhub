package com.sep490.vtuber_fanhub.repositories;

import com.sep490.vtuber_fanhub.models.PostMedia;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PostMediaRepository extends JpaRepository<PostMedia, Long> {

    List<PostMedia> findByPostId(Long postId);
}
