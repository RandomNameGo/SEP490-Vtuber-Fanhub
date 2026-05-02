package com.sep490.vtuber_fanhub.dto.responses;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.Instant;

@Data
@Builder
public class PaymentHistoryResponse {
    private Long id;
    private BigDecimal amount;
    private String description;
    private Instant createdAt;
    private String packageName;
    private String status;
}
