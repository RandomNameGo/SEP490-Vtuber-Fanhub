package com.sep490.vtuber_fanhub.repositories;

import com.sep490.vtuber_fanhub.models.FanHubJoinAnswer;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FanHubJoinAnswerRepository extends JpaRepository<FanHubJoinAnswer, Long> {
    
    @EntityGraph(attributePaths = {"question", "member", "member.hub"})
    List<FanHubJoinAnswer> findByMember_User_Id(Long userId);

    @EntityGraph(attributePaths = {"question", "member", "member.hub"})
    List<FanHubJoinAnswer> findByMember_User_IdAndMember_Hub_Id(Long userId, Long hubId);

    @EntityGraph(attributePaths = {"question"})
    List<FanHubJoinAnswer> findByMemberId(Long memberId);

    @EntityGraph(attributePaths = {"question", "member", "member.user"})
    List<FanHubJoinAnswer> findByMember_Hub_Id(Long hubId);

    @Modifying
    @Query("DELETE FROM FanHubJoinAnswer a WHERE a.member.id = :memberId")
    void deleteByMemberId(Long memberId);
}
