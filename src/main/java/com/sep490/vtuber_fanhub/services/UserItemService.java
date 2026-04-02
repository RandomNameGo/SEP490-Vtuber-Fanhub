package com.sep490.vtuber_fanhub.services;

import com.sep490.vtuber_fanhub.dto.requests.PurchaseItemRequest;
import com.sep490.vtuber_fanhub.dto.responses.PurchaseResponse;
import jakarta.servlet.http.HttpServletRequest;

public interface UserItemService {

    PurchaseResponse purchaseItem(PurchaseItemRequest request, HttpServletRequest httpRequest);
}
