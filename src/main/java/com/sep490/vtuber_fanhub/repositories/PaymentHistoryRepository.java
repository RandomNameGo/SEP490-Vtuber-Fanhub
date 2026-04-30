package com.sep490.vtuber_fanhub.repositories;

import com.sep490.vtuber_fanhub.models.PaymentHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface PaymentHistoryRepository extends JpaRepository<PaymentHistory, Long> {
    @Query("SELECT ph FROM PaymentHistory ph LEFT JOIN FETCH ph.user LEFT JOIN FETCH ph.packageField WHERE ph.id = :id")
    Optional<PaymentHistory> findByIdWithUserAndPackage(@Param("id") Long id);
}
