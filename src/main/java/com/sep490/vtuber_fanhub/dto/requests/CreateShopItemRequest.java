package com.sep490.vtuber_fanhub.dto.requests;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CreateShopItemRequest {

    @NotNull(message = "Item ID must not be null")
    private Long itemId;

    @NotNull(message = "Price must not be null")
    private Long price;
}
