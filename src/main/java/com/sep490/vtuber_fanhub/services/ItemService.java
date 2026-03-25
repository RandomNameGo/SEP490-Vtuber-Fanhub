package com.sep490.vtuber_fanhub.services;

import com.sep490.vtuber_fanhub.dto.requests.CreateItemRequest;

public interface ItemService {
    String createItem(CreateItemRequest request);
}
