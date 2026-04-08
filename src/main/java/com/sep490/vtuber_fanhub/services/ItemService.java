package com.sep490.vtuber_fanhub.services;

import com.sep490.vtuber_fanhub.dto.requests.CreateItemRequest;
import org.springframework.web.multipart.MultipartFile;

public interface ItemService {
    String createItem(CreateItemRequest request, MultipartFile image);
}
