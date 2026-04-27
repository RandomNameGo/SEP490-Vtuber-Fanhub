package com.sep490.vtuber_fanhub.repositories;

import com.sep490.vtuber_fanhub.models.FanHubJoinQuestion;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FanHubJoinQuestionRepository extends JpaRepository<FanHubJoinQuestion, Long> {
    
    @EntityGraph(attributePaths = {"hub"})
    @Query("SELECT q FROM FanHubJoinQuestion q WHERE q.hub.id = :hubId AND (q.isDeleted IS NULL OR q.isDeleted = false) ORDER BY q.orderNumber ASC")
    List<FanHubJoinQuestion> findActiveQuestionsByHubId(Long hubId);

    Optional<FanHubJoinQuestion> findByIdAndIsDeletedFalse(Long id);

    @Override
    @EntityGraph(attributePaths = {"hub"})
    @Query("SELECT q FROM FanHubJoinQuestion q WHERE q.id = :id AND (q.isDeleted IS NULL OR q.isDeleted = false)")
    Optional<FanHubJoinQuestion> findById(Long id);
}
