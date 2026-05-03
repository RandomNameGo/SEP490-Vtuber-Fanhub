package com.sep490.vtuber_fanhub.dto.requests;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class CreatePaidPackageRequest {

    @Size(max = 100, message = "Package name must not exceed 100 characters")
    @NotNull(message = "Package name must not be null")
    private String packageName;

    @NotNull(message = "Price must not be null")
    private BigDecimal price;

    @NotNull(message = "Paid points must not be null")
    private Long paidPoints;

    private String description;
}
