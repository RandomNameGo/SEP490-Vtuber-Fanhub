package com.sep490.vtuber_fanhub.services;

import com.sep490.vtuber_fanhub.dto.requests.CreateItemRequest;
import com.sep490.vtuber_fanhub.dto.requests.UpdateItemRequest;
import com.sep490.vtuber_fanhub.dto.responses.ItemResponse;
import com.sep490.vtuber_fanhub.models.Item;
import com.sep490.vtuber_fanhub.repositories.BannerItemRepository;
import com.sep490.vtuber_fanhub.repositories.ItemRepository;
import com.sep490.vtuber_fanhub.repositories.ShopItemRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ItemServiceImpl implements ItemService {

    private final ItemRepository itemRepository;

    private final ShopItemRepository shopItemRepository;

    private final BannerItemRepository bannerItemRepository;

    private final CloudinaryService cloudinaryService;

    @Override
    @Transactional
    public String createItem(CreateItemRequest request, MultipartFile image) {
        String imageUrl = null;
        if (image != null && !image.isEmpty()) {
            try {
                imageUrl = cloudinaryService.uploadFile(image);
            } catch (IOException e) {
                throw new RuntimeException("Failed to upload image", e);
            }
        }

        Item item = new Item();
        item.setItemName(request.getItemName());
        item.setDescription(request.getDescription());
        item.setImageUrl(imageUrl);
        item.setCategory(request.getCategory());
        item.setIsDeleted(false);

        itemRepository.save(item);

        return "Created item successfully";
    }

    @Override
    @Transactional(readOnly = true)
    public List<ItemResponse> getAllFrames() {
        return itemRepository.findActiveByCategory("FRAME").stream()
                .map(this::mapToItemResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<ItemResponse> getAllItems() {
        return itemRepository.findAllActive().stream()
                .map(this::mapToItemResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public String deleteItem(Long id) {
        Item item = itemRepository.findById(id)
                .orElseThrow(() -> new com.sep490.vtuber_fanhub.exceptions.NotFoundException("Item not found"));
        item.setIsDeleted(true);
        itemRepository.save(item);

        shopItemRepository.softDeleteByItemId(id);
        bannerItemRepository.deleteByItemId(id);

        return "Deleted item successfully";
    }

    @Override
    @Transactional
    public String updateItem(Long id, UpdateItemRequest request, MultipartFile image) {
        Item item = itemRepository.findById(id)
                .orElseThrow(() -> new com.sep490.vtuber_fanhub.exceptions.NotFoundException("Item not found"));

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

        return "Updated item successfully";
    }

    private ItemResponse mapToItemResponse(Item item) {
        ItemResponse response = new ItemResponse();
        response.setId(item.getId());
        response.setItemName(item.getItemName());
        response.setDescription(item.getDescription());
        response.setImageUrl(item.getImageUrl());
        response.setCategory(item.getCategory());
        return response;
    }
}
