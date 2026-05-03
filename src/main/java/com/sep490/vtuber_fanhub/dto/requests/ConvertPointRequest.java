package com.sep490.vtuber_fanhub.dto.requests;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ConvertPointRequest {
    @NotNull(message = "Amount must not be null")
    @Min(value = 1, message = "Amount must be at least 1")
    private Long amount;
}
