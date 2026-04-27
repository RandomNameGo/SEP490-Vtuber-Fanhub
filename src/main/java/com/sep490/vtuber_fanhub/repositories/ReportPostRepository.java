package com.sep490.vtuber_fanhub.repositories;

import com.sep490.vtuber_fanhub.models.ReportPost;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ReportPostRepository extends JpaRepository<ReportPost, Long> {

    @EntityGraph(attributePaths = {"post", "reportedBy"})
    @Query("SELECT rp FROM ReportPost rp JOIN rp.post p WHERE p.hub.id = :fanHubId")
    Page<ReportPost> findByFanHubId(@Param("fanHubId") Long fanHubId, Pageable pageable);

    @EntityGraph(attributePaths = {"post", "reportedBy"})
    Page<ReportPost> findByReportedById(Long reportedById, Pageable pageable);

    @EntityGraph(attributePaths = {"post", "reportedBy"})
    @Query("SELECT rp FROM ReportPost rp JOIN rp.post p WHERE p.hub.id = :fanHubId AND rp.status = :status")
    Page<ReportPost> findByFanHubIdAndStatus(@Param("fanHubId") Long fanHubId, @Param("status") String status, Pageable pageable);

    @EntityGraph(attributePaths = {"post", "reportedBy"})
    List<ReportPost> findByPostId(Long postId);
}