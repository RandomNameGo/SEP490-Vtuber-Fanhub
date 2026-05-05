package com.sep490.vtuber_fanhub.services;

import com.sep490.vtuber_fanhub.dto.requests.CreateBannerRequest;
import com.sep490.vtuber_fanhub.dto.responses.BannerDetailResponse;
import com.sep490.vtuber_fanhub.dto.responses.BannerItemResponse;
import com.sep490.vtuber_fanhub.dto.responses.BannerResponse;
import com.sep490.vtuber_fanhub.exceptions.NotFoundException;
import com.sep490.vtuber_fanhub.models.Banner;
import com.sep490.vtuber_fanhub.repositories.BannerItemRepository;
import com.sep490.vtuber_fanhub.repositories.BannerRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class BannerServiceImpl implements BannerService {

    private final BannerRepository bannerRepository;

    private final BannerItemRepository bannerItemRepository;

    private final BannerItemService bannerItemService;

    private final CloudinaryService cloudinaryService;

    @Override
    @Transactional
    public String createBanner(CreateBannerRequest request, MultipartFile bannerImage) {
        // Validate that end time is after start time
        if (!request.getEndTime().isAfter(request.getStartTime())) {
            throw new IllegalArgumentException("End time must be after start time");
        }

        // Check if there's an overlapping active banner
        List<Banner> overlappingBanners = bannerRepository.findOverlappingBanners(
                request.getStartTime(), request.getEndTime(), -1L);
        boolean isActive = overlappingBanners.isEmpty();

        // Upload banner image if provided
        String bannerImgUrl = null;
        if (bannerImage != null && !bannerImage.isEmpty()) {
            try {
                bannerImgUrl = cloudinaryService.uploadFile(bannerImage);
            } catch (IOException e) {
                throw new RuntimeException("Failed to upload banner image", e);
            }
        }

        Banner banner = new Banner();
        banner.setName(request.getName());
        banner.setStartTime(request.getStartTime());
        banner.setEndTime(request.getEndTime());
        banner.setDescription(request.getDescription());
        banner.setGachaCost(request.getGachaCost());
        banner.setCreatedAt(Instant.now());
        banner.setBannerImgUrl(bannerImgUrl);
        banner.setIsActive(isActive);

        bannerRepository.save(banner);

        return "Created banner successfully";
    }

    @Override
    @Transactional
    public String activateBanner(Long bannerId) {
        Banner banner = bannerRepository.findById(bannerId)
                .orElseThrow(() -> new NotFoundException("Banner not found"));

        if (Boolean.TRUE.equals(banner.getIsActive())) {
            return "Banner is already active";
        }

        // Deactivate any overlapping active banners
        List<Banner> overlappingBanners = bannerRepository.findOverlappingBanners(
                banner.getStartTime(), banner.getEndTime(), bannerId);
        
        for (Banner overlapping : overlappingBanners) {
            overlapping.setIsActive(false);
        }
        bannerRepository.saveAll(overlappingBanners);

        banner.setIsActive(true);
        bannerRepository.save(banner);

        return "Banner activated successfully";
    }

    @Override
    @Transactional
    public String deactivateBanner(Long bannerId) {
        Banner banner = bannerRepository.findById(bannerId)
                .orElseThrow(() -> new NotFoundException("Banner not found"));

        if (Boolean.FALSE.equals(banner.getIsActive())) {
            return "Banner is already inactive";
        }

        banner.setIsActive(false);
        bannerRepository.save(banner);

        return "Banner deactivated successfully";
    }

    @Override
    @Transactional(readOnly = true)
    public List<BannerResponse> getAllBanners(int pageNo, int pageSize, String sortBy) {
        Pageable paging = PageRequest.of(pageNo, pageSize, Sort.by(sortBy));

        Page<Banner> pagedBanners = bannerRepository.findAll(paging);

        if (pagedBanners.isEmpty()) {
            return List.of();
        }

        return pagedBanners.getContent().stream()
                .map(this::convertToResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public BannerResponse getActiveBanner() {
        Instant now = Instant.now();
        Banner banner = bannerRepository.findActiveBanner(now)
                .orElseThrow(() -> new NotFoundException("No active banner found"));

        return convertToResponse(banner);
    }

    @Override
    @Transactional(readOnly = true)
    public BannerDetailResponse getBannerDetail(Long bannerId) {
        Banner banner = bannerRepository.findById(bannerId)
                .orElseThrow(() -> new NotFoundException("Banner not found"));

        BannerDetailResponse response = convertToDetailResponse(banner);
        List<BannerItemResponse> items = bannerItemService.getBannerItemsByBannerId(bannerId, 0, Integer.MAX_VALUE, "id");
        response.setItems(items);

        return response;
    }

    @Override
    @Transactional
    public String deleteBanner(Long bannerId) {
        Banner banner = bannerRepository.findById(bannerId)
                .orElseThrow(() -> new NotFoundException("Banner not found"));

        // Delete all banner items first
        bannerItemRepository.deleteByBannerId(bannerId);

        // Delete the banner
        bannerRepository.delete(banner);

        return "Banner and its items deleted successfully";
    }


    @Override
    @Transactional
    @Scheduled(cron = "0 0 */12 * * *", zone = "Asia/Ho_Chi_Minh")
    public void deactivateExpiredBanners() {
        Instant now = Instant.now();
        List<Banner> expiredBanners = bannerRepository.findExpiredActiveBanners(now);

        if (!expiredBanners.isEmpty()) {
            log.info("Deactivating {} expired banners", expiredBanners.size());
            for (Banner banner : expiredBanners) {
                banner.setIsActive(false);
            }
            bannerRepository.saveAll(expiredBanners);
            log.info("Successfully deactivated {} expired banners", expiredBanners.size());
        }
    }

    private BannerResponse convertToResponse(Banner banner) {
        BannerResponse response = new BannerResponse();
        response.setBannerId(banner.getId());
        response.setName(banner.getName());
        response.setStartTime(banner.getStartTime());
        response.setEndTime(banner.getEndTime());
        response.setDescription(banner.getDescription());
        response.setGachaCost(banner.getGachaCost());
        response.setCreatedAt(banner.getCreatedAt());
        response.setBannerImgUrl(banner.getBannerImgUrl());
        response.setIsActive(banner.getIsActive());
        return response;
    }

    private BannerDetailResponse convertToDetailResponse(Banner banner) {
        BannerDetailResponse response = new BannerDetailResponse();
        response.setBannerId(banner.getId());
        response.setName(banner.getName());
        response.setStartTime(banner.getStartTime());
        response.setEndTime(banner.getEndTime());
        response.setDescription(banner.getDescription());
        response.setGachaCost(banner.getGachaCost());
        response.setCreatedAt(banner.getCreatedAt());
        response.setBannerImgUrl(banner.getBannerImgUrl());
        response.setIsActive(banner.getIsActive());
        return response;
    }
}
