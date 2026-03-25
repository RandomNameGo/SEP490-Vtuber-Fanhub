package com.sep490.vtuber_fanhub.repositories;

import com.sep490.vtuber_fanhub.models.BanMember;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BanMemberRepository extends JpaRepository<BanMember, Long> {
}
