package com.sep490.vtuber_fanhub.services;

import com.sep490.vtuber_fanhub.dto.responses.LoginResponse;
import com.sep490.vtuber_fanhub.exceptions.CustomAuthenticationException;
import com.sep490.vtuber_fanhub.models.User;
import com.sep490.vtuber_fanhub.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;


import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;

    private final PasswordEncoder passwordEncoder;

    private final JWTService jwtService;

    private final RefreshTokenService refreshTokenService;

    @Override
    public LoginResponse login(String username, String password) {
        Optional<User> user = userRepository.findByUsernameAndIsActive(username);
        if (user.isEmpty()) {
            throw new CustomAuthenticationException("Invalid username or password");
        }

        if(passwordEncoder.matches(password, user.get().getPasswordHash())){
            LoginResponse loginResponse = new LoginResponse();
            loginResponse.setId(user.get().getId());
            loginResponse.setUsername(user.get().getUsername());
            loginResponse.setToken(jwtService.generateToken(user.get()));
            loginResponse.setRefreshToken(refreshTokenService.createRefreshToken(user.get()));
            return loginResponse;
        } else {
            throw new CustomAuthenticationException("Invalid username or password");
        }
    }


}
