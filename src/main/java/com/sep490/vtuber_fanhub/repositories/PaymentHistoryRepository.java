package com.sep490.vtuber_fanhub.repositories;

import com.sep490.vtuber_fanhub.models.PaymentHistory;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PaymentHistoryRepository extends JpaRepository<PaymentHistory, Long> {
}
