package com.sep490.vtuber_fanhub.repositories;

import com.sep490.vtuber_fanhub.models.Banner;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BannerRepository extends JpaRepository<Banner, Long> {
}