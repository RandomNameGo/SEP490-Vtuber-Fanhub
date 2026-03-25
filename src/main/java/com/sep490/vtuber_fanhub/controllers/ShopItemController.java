package com.sep490.vtuber_fanhub.controllers;

import com.sep490.vtuber_fanhub.dto.requests.CreateShopItemRequest;
import com.sep490.vtuber_fanhub.dto.responses.APIResponse;
import com.sep490.vtuber_fanhub.dto.responses.ShopItemResponse;
import com.sep490.vtuber_fanhub.services.ShopItemService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("vhub/api/v1/shop-items")
@RequiredArgsConstructor
public class ShopItemController {

    private final ShopItemService shopItemService;

    @PostMapping("/add")
    @PreAuthorize("hasAnyRole('ADMIN', 'MODERATOR')")
    public ResponseEntity<?> createShopItem(@RequestBody @Valid CreateShopItemRequest request) {
        return ResponseEntity.ok().body(APIResponse.<String>builder()
                .success(true)
                .message("Success")
                .data(shopItemService.createShopItem(request))
                .build()
        );
    }

    @GetMapping("/all")
    public ResponseEntity<?> getAllShopItems(
            @RequestParam(defaultValue = "0") int pageNo,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(defaultValue = "shopItemId") String sortBy) {

        List<ShopItemResponse> items = shopItemService.getAllShopItems(pageNo, pageSize, sortBy);
        return ResponseEntity.ok().body(APIResponse.<List<ShopItemResponse>>builder()
                .success(true)
                .message("Success")
                .data(items)
                .build()
        );
    }
}
