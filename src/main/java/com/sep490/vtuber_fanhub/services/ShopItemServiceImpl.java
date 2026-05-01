package com.sep490.vtuber_fanhub.services;

import com.sep490.vtuber_fanhub.dto.requests.CreateShopItemRequest;
import com.sep490.vtuber_fanhub.dto.requests.UpdateShopItemRequest;
import com.sep490.vtuber_fanhub.dto.responses.ShopItemResponse;
import com.sep490.vtuber_fanhub.exceptions.NotFoundException;
import com.sep490.vtuber_fanhub.models.Item;
import com.sep490.vtuber_fanhub.models.ShopItem;
import com.sep490.vtuber_fanhub.repositories.ItemRepository;
import com.sep490.vtuber_fanhub.repositories.ShopItemRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ShopItemServiceImpl implements ShopItemService {

    private final ShopItemRepository shopItemRepository;

    private final ItemRepository itemRepository;

    private final CloudinaryService cloudinaryService;

    @Override
    @Transactional
    public String createShopItem(CreateShopItemRequest request, MultipartFile image) {
        String imageUrl = null;
        if (image != null && !image.isEmpty()) {
            try {
                imageUrl = cloudinaryService.uploadFile(image);
            } catch (IOException e) {
                throw new RuntimeException("Failed to upload image", e);
            }
        }

        Item item;

        // If itemId is provided, use existing item; otherwise create new item
        if (request.getItemId() != null) {
            item = itemRepository.findById(request.getItemId())
                    .orElseThrow(() -> new NotFoundException("Item not found"));
        } else {
            item = new Item();
            item.setItemName(request.getItemName());
            item.setDescription(request.getDescription());
            item.setImageUrl(imageUrl);
            item.setCategory(request.getCategory());
            item.setSize(request.getSize());
            item.setXAxis(request.getXAxis());
            item.setYAxis(request.getYAxis());
            itemRepository.save(item);
        }

        ShopItem shopItem = new ShopItem();
        shopItem.setItem(item);
        shopItem.setPrice(request.getPrice());
        shopItem.setIsDeleted(false);

        shopItemRepository.save(shopItem);

        return "Created shop item successfully";
    }

    @Override
    @Transactional(readOnly = true)
    public List<ShopItemResponse> getAllShopItems(int pageNo, int pageSize, String sortBy) {
        Pageable paging = PageRequest.of(pageNo, pageSize, Sort.by(sortBy));

        Page<ShopItem> pagedShopItems = shopItemRepository.findByIsDeletedFalse(paging);

        if (pagedShopItems.isEmpty()) {
            return List.of();
        }

        return pagedShopItems.getContent().stream()
                .map(this::convertToResponse)
                .toList();
    }

    @Override
    @Transactional
    public String updateShopItem(Long id, UpdateShopItemRequest request, MultipartFile image) {
        ShopItem shopItem = shopItemRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Shop item not found"));

        Item item = shopItem.getItem();
        item.setItemName(request.getItemName());
        item.setDescription(request.getDescription());
        item.setCategory(request.getCategory());
        item.setSize(request.getSize());
        item.setXAxis(request.getXAxis());
        item.setYAxis(request.getYAxis());

        if (image != null && !image.isEmpty()) {
            try {
                String imageUrl = cloudinaryService.uploadFile(image);
                item.setImageUrl(imageUrl);
            } catch (IOException e) {
                throw new RuntimeException("Failed to upload image", e);
            }
        }

        itemRepository.save(item);

        shopItem.setPrice(request.getPrice());
        shopItemRepository.save(shopItem);

        return "Updated shop item successfully";
    }

    @Override
    @Transactional
    public String deleteShopItem(Long id) {
        ShopItem shopItem = shopItemRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Shop item not found"));

        shopItem.setIsDeleted(true);
        shopItemRepository.save(shopItem);

        return "Deleted shop item successfully";
    }

    private ShopItemResponse convertToResponse(ShopItem shopItem) {
        ShopItemResponse response = new ShopItemResponse();
        response.setShopItemId(shopItem.getId());
        response.setItemId(shopItem.getItem().getId());
        response.setItemName(shopItem.getItem().getItemName());
        response.setDescription(shopItem.getItem().getDescription());
        response.setImageUrl(shopItem.getItem().getImageUrl());
        response.setCategory(shopItem.getItem().getCategory());
        response.setPrice(shopItem.getPrice());
        response.setSize(shopItem.getItem().getSize());
        response.setXAxis(shopItem.getItem().getXAxis());
        response.setYAxis(shopItem.getItem().getYAxis());
        return response;
    }
}
