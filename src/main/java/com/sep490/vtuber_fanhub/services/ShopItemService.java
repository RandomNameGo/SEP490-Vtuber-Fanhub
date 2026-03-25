package com.sep490.vtuber_fanhub.services;

import com.sep490.vtuber_fanhub.dto.requests.CreateShopItemRequest;
import com.sep490.vtuber_fanhub.dto.responses.ShopItemResponse;

import java.util.List;

public interface ShopItemService {
    String createShopItem(CreateShopItemRequest request);

    List<ShopItemResponse> getAllShopItems(int pageNo, int pageSize, String sortBy);
}
