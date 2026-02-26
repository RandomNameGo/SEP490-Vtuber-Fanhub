package com.sep490.vtuber_fanhub.repositories;

import com.sep490.vtuber_fanhub.models.FanHubCategory;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FanHubCategoryRepository extends JpaRepository<FanHubCategory, Long> {
}