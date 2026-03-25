package com.sep490.vtuber_fanhub.repositories;

import com.sep490.vtuber_fanhub.models.Item;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ItemRepository extends JpaRepository<Item, Long> {
}
