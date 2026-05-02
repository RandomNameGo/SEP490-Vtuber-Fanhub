package com.sep490.vtuber_fanhub.repositories;

import com.sep490.vtuber_fanhub.models.Item;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ItemRepository extends JpaRepository<Item, Long> {
    List<Item> findByCategory(String category);
    List<Item> findByIsDeletedFalseOrIsDeletedNull();
    List<Item> findByCategoryAndIsDeletedFalseOrIsDeletedNull(String category);
    
    @org.springframework.data.jpa.repository.Query("SELECT i FROM Item i WHERE (i.isDeleted IS NULL OR i.isDeleted = false)")
    List<Item> findAllActive();

    @org.springframework.data.jpa.repository.Query("SELECT i FROM Item i WHERE i.category = :category AND (i.isDeleted IS NULL OR i.isDeleted = false)")
    List<Item> findActiveByCategory(String category);
}
