package com.sep490.vtuber_fanhub.dto.responses;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class ItemResponse {
    private Long id;
    private String itemName;
    private String description;
    private String imageUrl;
    private String category;
    private BigDecimal size;
    private BigDecimal xAxis;
    private BigDecimal yAxis;
}
