package com.sep490.vtuber_fanhub.dto.requests;

import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class UpdatePaidPackageRequest {

    @Size(max = 100, message = "Package name must not exceed 100 characters")
    private String packageName;

    private BigDecimal price;

    private Long paidPoints;

    private String description;
}
