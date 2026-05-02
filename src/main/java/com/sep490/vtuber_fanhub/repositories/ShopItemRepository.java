package com.sep490.vtuber_fanhub.repositories;

import com.sep490.vtuber_fanhub.models.ShopItem;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ShopItemRepository extends JpaRepository<ShopItem, Long> {

    @Override
    @EntityGraph(attributePaths = {"item"})
    Page<ShopItem> findAll(Pageable pageable);

    @EntityGraph(attributePaths = {"item"})
    Page<ShopItem> findByIsDeletedFalse(Pageable pageable);

    @org.springframework.data.jpa.repository.Modifying
    @org.springframework.data.jpa.repository.Query("UPDATE ShopItem s SET s.isDeleted = true WHERE s.item.id = :itemId")
    void softDeleteByItemId(Long itemId);
}
