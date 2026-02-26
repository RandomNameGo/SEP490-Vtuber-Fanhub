package com.sep490.vtuber_fanhub.repositories;

import com.sep490.vtuber_fanhub.models.FanHub;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface FanHubRepository extends JpaRepository<FanHub, Long> {

    Optional<FanHub> findByOwnerUserId(Long ownerUserId);
}