package com.sep490.vtuber_fanhub.repositories;

import com.sep490.vtuber_fanhub.models.UserItem;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserItemRepository extends JpaRepository<UserItem, Long> {
}