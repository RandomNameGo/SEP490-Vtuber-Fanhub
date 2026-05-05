package com.sep490.vtuber_fanhub.repositories;

import com.sep490.vtuber_fanhub.models.Banner;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.Optional;

public interface BannerRepository extends JpaRepository<Banner, Long> {
    
    @Query("SELECT b FROM Banner b WHERE b.isActive = true")
    Optional<Banner> findActiveBanner(@Param("now") Instant now);

    @Query("SELECT b FROM Banner b WHERE b.isActive = true AND b.endTime < :now")
    java.util.List<Banner> findExpiredActiveBanners(@Param("now") Instant now);

    @Query("SELECT b FROM Banner b WHERE b.isActive = true AND b.startTime < :endTime AND b.endTime > :startTime AND b.id <> :bannerId")
    java.util.List<Banner> findOverlappingBanners(@Param("startTime") Instant startTime, @Param("endTime") Instant endTime, @Param("bannerId") Long bannerId);
}