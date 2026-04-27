package com.sep490.vtuber_fanhub.dto.responses;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class PaidPackageResponse {
    private Long id;
    private String packageName;
    private BigDecimal price;
    private Long paidPoints;
    private String description;
}
