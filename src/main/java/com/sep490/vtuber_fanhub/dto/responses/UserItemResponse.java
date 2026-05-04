package com.sep490.vtuber_fanhub.dto.responses;

import lombok.Data;

import java.math.BigDecimal;
import java.time.Instant;

@Data
public class UserItemResponse {

    private Long userItemId;
    private Long itemId;
    private String itemName;
    private String description;
    private String imageUrl;
    private BigDecimal size;
    private BigDecimal xAxis;
    private BigDecimal yAxis;
    private String category;
    private Instant obtainedAt;
}
