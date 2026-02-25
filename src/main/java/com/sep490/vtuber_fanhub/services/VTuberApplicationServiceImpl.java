package com.sep490.vtuber_fanhub.services;

import com.sep490.vtuber_fanhub.dto.requests.CreateVTuberApplication;
import com.sep490.vtuber_fanhub.exceptions.CustomAuthenticationException;
import com.sep490.vtuber_fanhub.exceptions.NotFoundException;
import com.sep490.vtuber_fanhub.models.User;
import com.sep490.vtuber_fanhub.models.VTuberApplication;
import com.sep490.vtuber_fanhub.repositories.UserRepository;
import com.sep490.vtuber_fanhub.repositories.VTuberApplicationRepository;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Objects;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class VTuberApplicationServiceImpl implements VTuberApplicationService {

    private final VTuberApplicationRepository vTuberApplicationRepository;

    private final UserRepository userRepository;

    private final HttpServletRequest httpServletRequest;

    private JWTService jwtService;

    @Override
    @Transactional
    public String createVTuberApplication(CreateVTuberApplication request) {


        String token = jwtService.getCurrentToken(httpServletRequest);

        String tokenUsername = jwtService.getUsernameFromToken(token);

        Optional<User> tokenUser = userRepository.findByUsernameAndIsActive(tokenUsername);
        if (tokenUser.isEmpty()) {
            throw new NotFoundException("User not found");
        }

        Optional<User> user = userRepository.findById(request.getUserId());
        if (user.isEmpty()) {
            throw new NotFoundException("User not found");
        }

        if(!Objects.equals(tokenUser.get().getId(), request.getUserId())) {
            throw new CustomAuthenticationException("Wrong credentials");
        }

        VTuberApplication application = new VTuberApplication();
        application.setUser(user.get());
        application.setChannelName(request.getChannelName());
        application.setChannelLink(request.getChannelLink());
        application.setStatus("PENDING");
        application.setCreatedAt(Instant.now());
        vTuberApplicationRepository.save(application);

        return "Submitted VTuber Application";
    }
}
