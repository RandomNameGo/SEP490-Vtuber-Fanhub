package com.sep490.vtuber_fanhub.repositories;

import com.sep490.vtuber_fanhub.models.UserFeedback;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserFeedbackRepository extends JpaRepository<UserFeedback, Long> {

    Page<UserFeedback> findByUserId(Long userId, Pageable pageable);
}