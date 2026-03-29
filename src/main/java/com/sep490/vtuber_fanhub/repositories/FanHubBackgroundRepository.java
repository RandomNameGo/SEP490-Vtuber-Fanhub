package com.sep490.vtuber_fanhub.repositories;

import com.sep490.vtuber_fanhub.models.FanHubBackground;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface FanHubBackgroundRepository extends JpaRepository<FanHubBackground, Long> {

    List<FanHubBackground> findByHubId(Long hubId);
    
    void deleteByHubId(Long hubId);
}
