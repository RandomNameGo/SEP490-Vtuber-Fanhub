package com.sep490.vtuber_fanhub.services;

import com.sep490.vtuber_fanhub.dto.requests.CreateUserRequest;
import com.sep490.vtuber_fanhub.models.User;
import com.sep490.vtuber_fanhub.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService{

    private final UserRepository userRepository;

    private final PasswordEncoder passwordEncoder;

    @Override
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

        return "Created user successfully";
    }
}
