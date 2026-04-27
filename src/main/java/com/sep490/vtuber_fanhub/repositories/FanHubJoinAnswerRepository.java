package com.sep490.vtuber_fanhub.repositories;

import com.sep490.vtuber_fanhub.models.FanHubJoinAnswer;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FanHubJoinAnswerRepository extends JpaRepository<FanHubJoinAnswer, Long> {
    
    @EntityGraph(attributePaths = {"question"})
    List<FanHubJoinAnswer> findByMemberId(Long memberId);

    @EntityGraph(attributePaths = {"question", "member", "member.user"})
    List<FanHubJoinAnswer> findByMember_Hub_Id(Long hubId);
}
