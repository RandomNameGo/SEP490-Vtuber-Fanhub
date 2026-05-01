package com.sep490.vtuber_fanhub.repositories;

import com.sep490.vtuber_fanhub.models.BannerItem;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.transaction.annotation.Transactional;

public interface BannerItemRepository extends JpaRepository<BannerItem, Long> {

    @EntityGraph(attributePaths = {"item", "banner"})
    Page<BannerItem> findByBannerId(Long bannerId, Pageable pageable);

    @Modifying
    @Transactional
    void deleteByBannerId(Long bannerId);
}