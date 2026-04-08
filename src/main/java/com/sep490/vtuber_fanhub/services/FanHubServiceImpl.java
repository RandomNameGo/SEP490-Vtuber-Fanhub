package com.sep490.vtuber_fanhub.services;

import com.sep490.vtuber_fanhub.dto.requests.CreateFanHubRequest;
import com.sep490.vtuber_fanhub.dto.requests.UpdateFanHubRequest;
import com.sep490.vtuber_fanhub.dto.responses.FanHubResponse;
import com.sep490.vtuber_fanhub.exceptions.CustomAuthenticationException;
import com.sep490.vtuber_fanhub.exceptions.NotFoundException;
import com.sep490.vtuber_fanhub.models.FanHub;
import com.sep490.vtuber_fanhub.models.FanHubBackground;
import com.sep490.vtuber_fanhub.models.FanHubCategory;
import com.sep490.vtuber_fanhub.models.FanHubMember;
import com.sep490.vtuber_fanhub.models.User;
import com.sep490.vtuber_fanhub.repositories.FanHubBackgroundRepository;
import com.sep490.vtuber_fanhub.repositories.FanHubCategoryRepository;
import com.sep490.vtuber_fanhub.repositories.FanHubMemberRepository;
import com.sep490.vtuber_fanhub.repositories.FanHubRepository;
import com.sep490.vtuber_fanhub.repositories.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.AccessDeniedException;
import java.time.Instant;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class FanHubServiceImpl implements FanHubService {

    private final FanHubRepository fanHubRepository;

    private final UserRepository userRepository;

    private final HttpServletRequest httpServletRequest;

    private final FanHubCategoryRepository fanHubCategoryRepository;

    private final CloudinaryService cloudinaryService;

    private final AuthService authService;

    private final FanHubMemberRepository fanHubMemberRepository;

    private final FanHubBackgroundRepository fanHubBackgroundRepository;

    @Override
    @Transactional
    public String createFanHub(CreateFanHubRequest request) {
        User currentUser = authService.getUserFromToken(httpServletRequest);


        FanHub fanHub = new FanHub();
        fanHub.setOwnerUser(currentUser);
        fanHub.setHubName(request.getHubName());
        fanHub.setSubdomain(request.getSubdomain());
        fanHub.setThemeColor(request.getThemeColor());
        fanHub.setDescription(request.getDescription());
        fanHub.setIsPrivate(request.getIsPrivate() != null ? request.getIsPrivate() : false);
        fanHub.setRequiresApproval(request.getRequiresApproval() != null ? request.getRequiresApproval() : false);
        fanHub.setIsActive(true);
        fanHub.setStrikeCount(0);
        fanHub.setCreatedAt(Instant.now());

        fanHubRepository.save(fanHub);

        if (request.getCategory() != null && !request.getCategory().isEmpty()) {
            for (String categoryName : request.getCategory()) {
                FanHubCategory category = new FanHubCategory();
                category.setHub(fanHub);
                category.setCategoryName(categoryName);
                fanHubCategoryRepository.save(category);
            }
        }

        return "Created FanHub successfully";
    }

    @Override
    @Transactional
    public String updateFanHub(Long fanHubId, UpdateFanHubRequest request) {
        User currentUser = authService.getUserFromToken(httpServletRequest);

        Optional<FanHub> fanHubOptional = fanHubRepository.findById(fanHubId);
        if (fanHubOptional.isEmpty()) {
            throw new NotFoundException("FanHub not found");
        }

        FanHub fanHub = fanHubOptional.get();

        if (!fanHub.getOwnerUser().getId().equals(currentUser.getId())) {
            throw new CustomAuthenticationException("Access denied. Only the owner can update this FanHub.");
        }

        if (request.getHubName() != null) {
            fanHub.setHubName(request.getHubName());
        }

        if (request.getSubdomain() != null) {
            if (!fanHub.getSubdomain().equals(request.getSubdomain())) {
                if (fanHubRepository.existsBySubdomainAndIsActive(request.getSubdomain(), true)) {
                    throw new IllegalArgumentException("Subdomain is already in use");
                }
            }
            fanHub.setSubdomain(request.getSubdomain());
        }

        if (request.getDescription() != null) {
            fanHub.setDescription(request.getDescription());
        }

        if (request.getThemeColor() != null) {
            fanHub.setThemeColor(request.getThemeColor());
        }

        if (request.getIsPrivate() != null) {
            fanHub.setIsPrivate(request.getIsPrivate());
        }

        if (request.getRequiresApproval() != null) {
            fanHub.setRequiresApproval(request.getRequiresApproval());
        }

        if (request.getCategory() != null) {
            fanHubCategoryRepository.deleteByHubId(fanHubId);
            
            for (String categoryName : request.getCategory()) {
                FanHubCategory category = new FanHubCategory();
                category.setHub(fanHub);
                category.setCategoryName(categoryName);
                fanHubCategoryRepository.save(category);
            }
        }

        fanHubRepository.save(fanHub);

        return "Updated FanHub successfully";
    }

    @Override
    @Transactional
    public String uploadFanHubBannerBackGroundAvatar(long fanHubId, MultipartFile banner, List<MultipartFile> highlight, MultipartFile avatar) throws IOException {
        User currentUser = authService.getUserFromToken(httpServletRequest);

        Optional<FanHub> fanHub = fanHubRepository.findById(fanHubId);
        if (fanHub.isEmpty()) {
            throw new NotFoundException("FanHub not found");
        }

        if (!fanHub.get().getOwnerUser().getId().equals(currentUser.getId())) {
            throw new AccessDeniedException("Access denied");
        }

        if (banner != null && !banner.isEmpty()) {
            String bannerUrl = cloudinaryService.uploadFile(banner);
            fanHub.get().setBannerUrl(bannerUrl);
        }

        if (highlight != null && !highlight.isEmpty()) {
            if (highlight.size() > 4) {
                throw new IllegalArgumentException("Maximum 4 background images are allowed");
            }
            
            fanHubBackgroundRepository.deleteByHubId(fanHubId);
            
            for (MultipartFile background : highlight) {
                if (background != null && !background.isEmpty()) {
                    String backgroundUrl = cloudinaryService.uploadFile(background);
                    
                    FanHubBackground fanHubBackground = new FanHubBackground();
                    fanHubBackground.setHub(fanHub.get());
                    fanHubBackground.setImageUrl(backgroundUrl);
                    fanHubBackgroundRepository.save(fanHubBackground);
                }
            }
        }

        if (avatar != null && !avatar.isEmpty()) {
            String avatarUrl = cloudinaryService.uploadFile(avatar);
            fanHub.get().setAvatarUrl(avatarUrl);
        }

        fanHubRepository.save(fanHub.get());

        return "Uploaded FanHub images successfully";
    }

    @Override
    @Transactional(readOnly = true)
    public List<FanHubResponse> getAllFanHubs(int pageNo, int pageSize, String sortBy, boolean includePrivate) {
        Pageable paging = PageRequest.of(pageNo, pageSize, Sort.by(sortBy));

        Page<FanHub> pagedFanHubs;
        if (includePrivate) {
            // Include private FanHubs (for authenticated users, e.g., VTUBER or MODERATOR)
            pagedFanHubs = fanHubRepository.findAllActiveFanHubs(paging);
        } else {
            // Only public FanHubs
            pagedFanHubs = fanHubRepository.findActivePublicFanHubs(paging);
        }

        if (pagedFanHubs.isEmpty()) {
            return List.of();
        }

        return pagedFanHubs.getContent().stream()
                .map(this::mapToFanHubResponseWithMemberCount)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<FanHubResponse> getTopFanHubs(int pageNo, int pageSize, String category) {
        Pageable paging = PageRequest.of(pageNo, pageSize);

        List<FanHub> fanHubs;
        if (category == null || category.isEmpty()) {
            fanHubs = fanHubRepository.findTopFanHubsByMemberCount(paging);
        } else {
            fanHubs = fanHubRepository.findTopFanHubsByMemberCountAndCategory(category, paging);
        }

        if (fanHubs.isEmpty()) {
            return List.of();
        }

        return fanHubs.stream()
                .map(this::mapToFanHubResponseWithMemberCount)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public FanHubResponse getFanHubBySubdomain(String subdomain) {
        FanHub fanHub = fanHubRepository.findBySubdomainAndIsActive(subdomain, true)
                .orElseThrow(() -> new NotFoundException("FanHub not found with subdomain: " + subdomain));

        return mapToFanHubResponseWithMemberCount(fanHub);
    }

    @Override
    @Transactional(readOnly = true)
    public List<FanHubResponse> getJoinedFanHubs(int pageNo, int pageSize, String sortBy) {
        User currentUser = authService.getUserFromToken(httpServletRequest);

        Pageable paging = PageRequest.of(pageNo, pageSize, Sort.by(sortBy));

        List<FanHubMember> joinedMembers = fanHubMemberRepository.findByUserIdAndStatus(currentUser.getId(), "JOINED");

        if (joinedMembers.isEmpty()) {
            return List.of();
        }

        // Apply pagination manually since we need to filter by status
        int start = Math.min(pageNo * pageSize, joinedMembers.size());
        int end = Math.min(start + pageSize, joinedMembers.size());

        if (start >= joinedMembers.size()) {
            return List.of();
        }

        List<FanHubMember> pagedMembers = joinedMembers.subList(start, end);

        return pagedMembers.stream()
                .map(member -> mapToFanHubResponseWithMemberCount(member.getHub()))
                .collect(Collectors.toList());
    }

    private FanHubResponse mapToFanHubResponse(FanHub fanHub) {
        FanHubResponse response = new FanHubResponse();
        response.setFanHubId(fanHub.getId());
        response.setSubdomain(fanHub.getSubdomain());
        response.setHubName(fanHub.getHubName());
        response.setDescription(fanHub.getDescription());
        response.setBannerUrl(fanHub.getBannerUrl());
        
        List<String> backgroundUrls = fanHubBackgroundRepository.findByHubId(fanHub.getId())
                .stream()
                .map(FanHubBackground::getImageUrl)
                .collect(Collectors.toList());
        response.setHighlightImgUrls(backgroundUrls);
        response.setBackgroundUrl(fanHub.getBackgroundUrl());
        
        response.setThemeColor(fanHub.getThemeColor());
        response.setAvatarUrl(fanHub.getAvatarUrl());
        response.setIsPrivate(fanHub.getIsPrivate());
        response.setRequiresApproval(fanHub.getRequiresApproval());
        response.setCreatedAt(fanHub.getCreatedAt());

        User owner = fanHub.getOwnerUser();
        response.setOwnerUserId(owner.getId());
        response.setOwnerUsername(owner.getUsername());
        response.setOwnerDisplayName(owner.getDisplayName());

        List<String> categories = fanHubCategoryRepository.findByHubId(fanHub.getId())
                .stream()
                .map(FanHubCategory::getCategoryName)
                .collect(Collectors.toList());
        response.setCategories(categories);

        return response;
    }

    private FanHubResponse mapToFanHubResponseWithMemberCount(FanHub fanHub) {
        FanHubResponse response = mapToFanHubResponse(fanHub);
        
        long memberCount = fanHubMemberRepository.findByHubIdAndStatus(fanHub.getId(), "JOINED", Pageable.unpaged())
                .getTotalElements();
        response.setMemberCount(memberCount);

        return response;
    }
}
