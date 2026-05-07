package com.sep490.vtuber_fanhub.repositories;

import com.sep490.vtuber_fanhub.models.UserDailyMission;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface UserDailyMissionRepository extends JpaRepository<UserDailyMission, Long> {

    @Query("SELECT udm FROM UserDailyMission udm JOIN FETCH udm.user WHERE udm.user.id = :userId")
    Optional<UserDailyMission> findByUserIdWithUser(@Param("userId") Long userId);

    Optional<UserDailyMission> findByUserId(Long userId);

}