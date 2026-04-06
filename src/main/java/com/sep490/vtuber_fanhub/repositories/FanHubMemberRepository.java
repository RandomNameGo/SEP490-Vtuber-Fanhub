package com.sep490.vtuber_fanhub.repositories;

import com.sep490.vtuber_fanhub.models.FanHubMember;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface FanHubMemberRepository extends JpaRepository<FanHubMember, Long> {

    Page<FanHubMember> findByHubIdAndStatus(Long fanHubId, String status, Pageable pageable);

    Page<FanHubMember> findByHubId(Long fanHubId, Pageable pageable);

    @Query("select f from FanHubMember f where f.hub.id = :fanHubId and f.user.username = :username")
    Page<FanHubMember> findByHubIdAndUsername(@Param("fanHubId") Long fanHubId, @Param("username") String username, Pageable pageable);

    Optional<FanHubMember> findByHubIdAndUserId(Long fanHubId, Long userId);

    Optional<FanHubMember> findByHub_IdAndUser_Id(Long fanHubId, Long userId);

    List<FanHubMember> findAllByUserId(Long userId);

    long countByUserId(Long userId);
}