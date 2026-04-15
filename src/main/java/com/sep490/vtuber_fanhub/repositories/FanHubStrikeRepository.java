package com.sep490.vtuber_fanhub.repositories;

import com.sep490.vtuber_fanhub.models.FanHubStrike;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface FanHubStrikeRepository extends JpaRepository<FanHubStrike, Long> {
}
