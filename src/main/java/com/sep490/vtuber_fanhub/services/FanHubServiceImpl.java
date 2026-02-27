package com.sep490.vtuber_fanhub.services;

import com.sep490.vtuber_fanhub.dto.requests.CreateFanHubRequest;
import com.sep490.vtuber_fanhub.exceptions.CustomAuthenticationException;
import com.sep490.vtuber_fanhub.exceptions.NotFoundException;
import com.sep490.vtuber_fanhub.models.FanHub;
import com.sep490.vtuber_fanhub.models.FanHubCategory;
import com.sep490.vtuber_fanhub.models.User;
import com.sep490.vtuber_fanhub.repositories.FanHubCategoryRepository;
import com.sep490.vtuber_fanhub.repositories.FanHubRepository;
import com.sep490.vtuber_fanhub.repositories.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.AccessDeniedException;
import java.time.Instant;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class FanHubServiceImpl implements FanHubService {

    private final FanHubRepository fanHubRepository;

    private final UserRepository userRepository;

    private final HttpServletRequest httpServletRequest;

    private final JWTService jwtService;

    private final FanHubCategoryRepository fanHubCategoryRepository;

    private final CloudinaryService cloudinaryService;

    @Override
    @Transactional
    public String createFanHub(CreateFanHubRequest request) {

        String token = jwtService.getCurrentToken(httpServletRequest);

        String tokenUsername = jwtService.getUsernameFromToken(token);

        Optional<User> tokenUser = userRepository.findByUsernameAndIsActive(tokenUsername);
        if (tokenUser.isEmpty()) {
            throw new CustomAuthenticationException("Authentication failed");
        }

        FanHub fanHub = new FanHub();
        fanHub.setOwnerUser(tokenUser.get());
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
    public String uploadFanHubBannerBackGroundAvatar(long fanHubId, MultipartFile banner, MultipartFile background, MultipartFile avatar) throws IOException {

        String token = jwtService.getCurrentToken(httpServletRequest);

        String tokenUsername = jwtService.getUsernameFromToken(token);

        Optional<User> tokenUser = userRepository.findByUsernameAndIsActive(tokenUsername);
        if (tokenUser.isEmpty()) {
            throw new CustomAuthenticationException("Authentication failed");
        }

        Optional<FanHub> fanHub = fanHubRepository.findById(fanHubId);
        if (fanHub.isEmpty()) {
            throw new NotFoundException("FanHub not found");
        }

        if (!fanHub.get().getOwnerUser().getId().equals(tokenUser.get().getId())) {
            throw new AccessDeniedException("Access denied");
        }

        if (banner != null && !banner.isEmpty()) {
            String bannerUrl = cloudinaryService.uploadFile(banner);
            fanHub.get().setBannerUrl(bannerUrl);
        }

        if (background != null && !background.isEmpty()) {
            String backgroundUrl = cloudinaryService.uploadFile(background);
            fanHub.get().setBackgroundUrl(backgroundUrl);
        }

        if (avatar != null && !avatar.isEmpty()) {
            String avatarUrl = cloudinaryService.uploadFile(avatar);
            fanHub.get().setAvatarUrl(avatarUrl);
        }

        fanHubRepository.save(fanHub.get());

        return "Uploaded FanHub images successfully";
    }
}
