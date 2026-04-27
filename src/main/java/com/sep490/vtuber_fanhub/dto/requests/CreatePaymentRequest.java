package com.sep490.vtuber_fanhub.dto.requests;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class CreatePaymentRequest {

    private long userId;

    private long paidPackageId;

    private String paidPackageName;

    private String paidPackageDescription;

    private BigDecimal price;

    private String returnUrl;

    private String cancelUrl;
}
