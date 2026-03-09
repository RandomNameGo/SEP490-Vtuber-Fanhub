package com.sep490.vtuber_fanhub.repositories;

import com.sep490.vtuber_fanhub.models.FanHubMember;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface FanHubMemberRepository extends JpaRepository<FanHubMember, Long> {

    Page<FanHubMember> findByHubIdAndStatus(Long fanHubId, String status, Pageable pageable);

    Page<FanHubMember> findByHubId(Long fanHubId, Pageable pageable);

    Optional<FanHubMember> findByHubIdAndUserId(Long fanHubId, Long userId);

    List<FanHubMember> findAllByUserId(Long userId);
}