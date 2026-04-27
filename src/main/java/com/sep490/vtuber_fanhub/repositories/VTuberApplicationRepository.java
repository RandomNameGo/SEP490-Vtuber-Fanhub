package com.sep490.vtuber_fanhub.repositories;

import com.sep490.vtuber_fanhub.models.VTuberApplication;
import org.springframework.data.jpa.repository.JpaRepository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.util.List;

public interface VTuberApplicationRepository extends JpaRepository<VTuberApplication, Long> {
    Page<VTuberApplication> findByUserId(Long userId, Pageable pageable);
}