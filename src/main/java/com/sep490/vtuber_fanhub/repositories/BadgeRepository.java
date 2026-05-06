package com.sep490.vtuber_fanhub.repositories;

import com.sep490.vtuber_fanhub.models.Badge;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BadgeRepository extends JpaRepository<Badge, Long> {
    List<Badge> findByTypeAndLikeRequireLessThanEqual(String type, Long likeRequire);
    List<Badge> findByTypeAndCommentRequireLessThanEqual(String type, Long commentRequire);
    List<Badge> findByType(String type);
}
