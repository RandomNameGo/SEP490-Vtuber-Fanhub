package com.sep490.vtuber_fanhub.services;

import com.sep490.vtuber_fanhub.dto.requests.CreateBannerRequest;
import com.sep490.vtuber_fanhub.dto.responses.BannerResponse;
import com.sep490.vtuber_fanhub.models.Banner;
import com.sep490.vtuber_fanhub.repositories.BannerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

@Service
@RequiredArgsConstructor
public class BannerServiceImpl implements BannerService {

    private final BannerRepository bannerRepository;

    @Override
    @Transactional
    public String createBanner(CreateBannerRequest request) {
        Banner banner = new Banner();
        banner.setName(request.getName());
        banner.setStartTime(request.getStartTime());
        banner.setEndTime(request.getEndTime());
        banner.setDescription(request.getDescription());
        banner.setGachaCost(request.getGachaCost());
        banner.setCreatedAt(Instant.now());

        bannerRepository.save(banner);

        return "Created banner successfully";
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

    private BannerResponse convertToResponse(Banner banner) {
        BannerResponse response = new BannerResponse();
        response.setBannerId(banner.getId());
        response.setName(banner.getName());
        response.setStartTime(banner.getStartTime());
        response.setEndTime(banner.getEndTime());
        response.setDescription(banner.getDescription());
        response.setGachaCost(banner.getGachaCost());
        response.setCreatedAt(banner.getCreatedAt());
        return response;
    }
}
