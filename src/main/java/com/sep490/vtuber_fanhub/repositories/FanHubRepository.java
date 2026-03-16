package com.sep490.vtuber_fanhub.repositories;

import com.sep490.vtuber_fanhub.models.FanHub;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface FanHubRepository extends JpaRepository<FanHub, Long> {

    Optional<FanHub> findByOwnerUserId(Long ownerUserId);
    
    @Query("SELECT fh FROM FanHub fh WHERE fh.isActive = true AND fh.isPrivate = false ORDER BY fh.createdAt DESC")
    Page<FanHub> findActivePublicFanHubs(Pageable pageable);
    
    @Query("SELECT fh FROM FanHub fh WHERE fh.isActive = true ORDER BY fh.createdAt DESC")
    Page<FanHub> findAllActiveFanHubs(Pageable pageable);
}