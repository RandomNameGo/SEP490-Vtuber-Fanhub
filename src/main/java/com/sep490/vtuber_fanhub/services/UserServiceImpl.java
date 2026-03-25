package com.sep490.vtuber_fanhub.services;

import com.sep490.vtuber_fanhub.dto.requests.CreateUserRequest;
import com.sep490.vtuber_fanhub.dto.requests.UpdateUserRequest;
import com.sep490.vtuber_fanhub.dto.responses.UserResponse;
import com.sep490.vtuber_fanhub.exceptions.NotFoundException;
import com.sep490.vtuber_fanhub.models.User;
import com.sep490.vtuber_fanhub.models.UserBadge;
import com.sep490.vtuber_fanhub.models.UserDailyMission;
import com.sep490.vtuber_fanhub.repositories.UserBadgeRepository;
import com.sep490.vtuber_fanhub.repositories.UserDailyMissionRepository;
import com.sep490.vtuber_fanhub.repositories.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService{

    private final UserRepository userRepository;

    private final PasswordEncoder passwordEncoder;

    private final HttpServletRequest httpServletRequest;

    private final CloudinaryService cloudinaryService;

    private final UserDailyMissionRepository userDailyMissionRepository;

    private final AuthService authService;

    private final UserBadgeRepository userBadgeRepository;

    @Override
    @Transactional
    public String createUser(CreateUserRequest createUserRequest) {

        if(userRepository.existsByUsername(createUserRequest.getUsername())){
            return "Username is already in use";
        }

        if(userRepository.existsByEmail(createUserRequest.getEmail())){
            return "Email is already in use";
        }

        User user = new User();
        user.setUsername(createUserRequest.getUsername());
        user.setEmail(createUserRequest.getEmail());
        user.setPasswordHash(passwordEncoder.encode(createUserRequest.getPassword()));
        user.setDisplayName(createUserRequest.getDisplayName());
        user.setBio(createUserRequest.getBio());

        user.setPoints(0L);
        user.setPaidPoints(0L);
        user.setIsActive(true);
        user.setCreatedAt(Instant.now());
        user.setUpdatedAt(Instant.now());
        user.setRole("USER");

        userRepository.save(user);

        UserDailyMission userDailyMission = new UserDailyMission();
        userDailyMission.setUser(user);
        userDailyMission.setLikeAmount(0);
        userDailyMissionRepository.save(userDailyMission);

        return "Created user successfully";
    }

    @Override
    @Transactional
    public String uploadAvatarFrame(MultipartFile avatarFile, MultipartFile frameFile) throws IOException {
        User currentUser = authService.getUserFromToken(httpServletRequest);

        if(!avatarFile.isEmpty()){
            String avatarUrl = cloudinaryService.uploadFile(avatarFile);
            currentUser.setAvatarUrl(avatarUrl);
        }
        if(!frameFile.isEmpty()){
            String frameUrl = cloudinaryService.uploadFile(frameFile);
            currentUser.setFrameUrl(frameUrl);
        }

        return "Uploaded successfully";
    }

    @Override
    @Transactional
    public String updateUser(UpdateUserRequest updateUserRequest) {

        User user = authService.getUserFromToken(httpServletRequest);

        if (updateUserRequest.getEmail() != null && !updateUserRequest.getEmail().isEmpty()) {
            if (!user.getEmail().equals(updateUserRequest.getEmail()) && userRepository.existsByEmail(updateUserRequest.getEmail())) {
                return "Email is already in use";
            }
            user.setEmail(updateUserRequest.getEmail());
        }

        if (updateUserRequest.getDisplayName() != null) {
            user.setDisplayName(updateUserRequest.getDisplayName());
        }

        if (updateUserRequest.getTranslateLanguage() != null) {
            user.setTranslateLanguage(updateUserRequest.getTranslateLanguage());
        }

        if (updateUserRequest.getBio() != null) {
            user.setBio(updateUserRequest.getBio());
        }

        user.setUpdatedAt(Instant.now());

        userRepository.save(user);

        return "Updated user successfully";
    }

    @Override
    @Transactional(readOnly = true)
    public UserResponse getUserDetailWithBadge(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User not found with id: " + userId));

        List<UserBadge> userBadges = userBadgeRepository.findByUserId(userId);

        return mapToUserResponse(user, userBadges);
    }

    private UserResponse mapToUserResponse(User user, List<UserBadge> userBadges) {
        UserResponse response = new UserResponse();

        response.setUserId(user.getId());
        response.setUsername(user.getUsername());
        response.setEmail(user.getEmail());
        response.setDisplayName(user.getDisplayName());
        response.setAvatarUrl(user.getAvatarUrl());
        response.setFrameUrl(user.getFrameUrl());
        response.setBio(user.getBio());
        response.setRole(user.getRole());
        response.setPoints(user.getPoints());
        response.setPaidPoints(user.getPaidPoints());
        response.setTranslateLanguage(user.getTranslateLanguage());
        response.setCreatedAt(user.getCreatedAt());
        response.setUpdatedAt(user.getUpdatedAt());
        response.setIsActive(user.getIsActive());

        if (userBadges != null && !userBadges.isEmpty()) {
            List<UserResponse.UserBadgeResponse> badgeResponses = new ArrayList<>();
            for (UserBadge userBadge : userBadges) {
                UserResponse.UserBadgeResponse badgeResponse = new UserResponse.UserBadgeResponse();
                badgeResponse.setUserBadgeId(userBadge.getId());
                badgeResponse.setBadgeId(userBadge.getBadge().getId());
                badgeResponse.setBadgeName(userBadge.getBadge().getBadgeName());
                badgeResponse.setDescription(userBadge.getBadge().getDescription());
                badgeResponse.setIconUrl(userBadge.getBadge().getIconUrl());
                badgeResponse.setRequirement(userBadge.getBadge().getRequirement());
                badgeResponse.setAcquiredAt(userBadge.getAcquiredAt());
                badgeResponses.add(badgeResponse);
            }
            response.setBadges(badgeResponses);
        }

        return response;
    }
}
