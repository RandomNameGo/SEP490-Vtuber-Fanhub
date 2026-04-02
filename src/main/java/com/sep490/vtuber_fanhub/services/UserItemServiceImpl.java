package com.sep490.vtuber_fanhub.services;

import com.sep490.vtuber_fanhub.dto.requests.PurchaseItemRequest;
import com.sep490.vtuber_fanhub.dto.responses.PurchaseResponse;
import com.sep490.vtuber_fanhub.exceptions.NotFoundException;
import com.sep490.vtuber_fanhub.models.Item;
import com.sep490.vtuber_fanhub.models.ShopItem;
import com.sep490.vtuber_fanhub.models.User;
import com.sep490.vtuber_fanhub.models.UserItem;
import com.sep490.vtuber_fanhub.repositories.ItemRepository;
import com.sep490.vtuber_fanhub.repositories.ShopItemRepository;
import com.sep490.vtuber_fanhub.repositories.UserItemRepository;
import com.sep490.vtuber_fanhub.repositories.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserItemServiceImpl implements UserItemService {

    private final UserItemRepository userItemRepository;
    private final ShopItemRepository shopItemRepository;
    private final UserRepository userRepository;
    private final ItemRepository itemRepository;
    private final AuthService authService;

    @Override
    @Transactional
    public PurchaseResponse purchaseItem(PurchaseItemRequest request, HttpServletRequest httpRequest) {
        User user = authService.getUserFromToken(httpRequest);

        ShopItem shopItem = shopItemRepository.findById(request.getShopItemId())
                .orElseThrow(() -> new NotFoundException("Shop item not found"));

        Item item = shopItem.getItem();
        Long price = shopItem.getPrice();

        Long userPaidPoints = user.getPaidPoints() != null ? user.getPaidPoints() : 0L;
        
        if (userPaidPoints < price) {
            throw new IllegalStateException("Insufficient points. Required: " + price + ", Available: " + userPaidPoints);
        }

        user.setPaidPoints(userPaidPoints - price);
        userRepository.save(user);

        UserItem userItem = new UserItem();
        userItem.setUser(user);
        userItem.setItem(item);
        userItem.setPurchasedAt(Instant.now());
        userItemRepository.save(userItem);

        log.info("User {} purchased item {} for {} points", user.getId(), item.getId(), price);

        return convertToResponse(userItem, price);
    }

    private PurchaseResponse convertToResponse(UserItem userItem, Long price) {
        PurchaseResponse response = new PurchaseResponse();
        response.setUserItemId(userItem.getId());
        response.setUserId(userItem.getUser().getId());
        response.setItemId(userItem.getItem().getId());
        response.setItemName(userItem.getItem().getItemName());
        response.setPrice(price);
        response.setPurchasedAt(userItem.getPurchasedAt());
        return response;
    }
}
