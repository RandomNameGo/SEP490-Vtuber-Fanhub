package com.sep490.vtuber_fanhub.services;

import com.sep490.vtuber_fanhub.dto.requests.CreateUserRequest;
import com.sep490.vtuber_fanhub.dto.requests.UpdateUserRequest;
import com.sep490.vtuber_fanhub.models.User;
import com.sep490.vtuber_fanhub.models.UserDailyMission;
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

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService{

    private final UserRepository userRepository;

    private final PasswordEncoder passwordEncoder;

    private final HttpServletRequest httpServletRequest;

    private final CloudinaryService cloudinaryService;

    private final UserDailyMissionRepository userDailyMissionRepository;

    private final AuthService authService;

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
}
