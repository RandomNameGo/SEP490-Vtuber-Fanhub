package com.sep490.vtuber_fanhub.services;

import com.sep490.vtuber_fanhub.dto.requests.CreateVTuberApplication;
import com.sep490.vtuber_fanhub.dto.responses.VTuberApplicationResponse;
import com.sep490.vtuber_fanhub.exceptions.CustomAuthenticationException;
import com.sep490.vtuber_fanhub.exceptions.NotFoundException;
import com.sep490.vtuber_fanhub.models.SystemAccount;
import com.sep490.vtuber_fanhub.models.User;
import com.sep490.vtuber_fanhub.models.VTuberApplication;
import com.sep490.vtuber_fanhub.repositories.SystemAccountRepository;
import com.sep490.vtuber_fanhub.repositories.UserRepository;
import com.sep490.vtuber_fanhub.repositories.VTuberApplicationRepository;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;


@Service
@RequiredArgsConstructor
@Slf4j
public class VTuberApplicationServiceImpl implements VTuberApplicationService {

    private final VTuberApplicationRepository vTuberApplicationRepository;

    private final UserRepository userRepository;

    private final HttpServletRequest httpServletRequest;

    private final SystemAccountRepository systemAccountRepository;

    private final AuthService authService;

    private final JWTService jwtService;

    private final YoutubeAPIService youtubeAPIService;

    private final NotificationService notificationService;

    @Override
    @Transactional
    public String createVTuberApplication(CreateVTuberApplication request) throws ExecutionException, InterruptedException {
        User currentUser = authService.getUserFromToken(httpServletRequest);

        Optional<User> user = userRepository.findById(request.getUserId());
        if (user.isEmpty()) {
            throw new NotFoundException("User not found");
        }

        if(!Objects.equals(currentUser.getId(), request.getUserId())) {
            throw new CustomAuthenticationException("Wrong credentials");
        }

        VTuberApplication application = new VTuberApplication();
        application.setUser(user.get());
        application.setChannelName(request.getChannelName());
        application.setChannelLink(request.getChannelLink());
        application.setStatus("PENDING");
        application.setCreatedAt(Instant.now());
        application.setChannelId(request.getChannelId());
        VTuberApplication vTuberApplication = vTuberApplicationRepository.save(application);
        validateChannelId(vTuberApplication);

        return "Submitted VTuber Application";
    }

    @Override
    public List<VTuberApplicationResponse> getAllVTuberApplications(int pageNo, int pageSize, String sortBy, String sortDir) {

        Sort sort = getSortDirection(sortDir).equalsIgnoreCase("asc") ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
        Pageable paging = PageRequest.of(pageNo, pageSize, sort);

        Page<VTuberApplication> pagedVTuberApplications = vTuberApplicationRepository.findAll(paging);

        if (pagedVTuberApplications.hasContent()) {
            return pagedVTuberApplications.getContent().stream()
                    .map(this::mapToResponse)
                    .collect(Collectors.toList());
        }


        return List.of();
    }

    @Override
    public List<VTuberApplicationResponse> getMyVTuberApplications(int pageNo, int pageSize, String sortBy, String sortDir) {
        User currentUser = authService.getUserFromToken(httpServletRequest);
        Sort sort = getSortDirection(sortDir).equalsIgnoreCase("asc") ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
        Pageable paging = PageRequest.of(pageNo, pageSize, sort);
        Page<VTuberApplication> pagedVTuberApplications = vTuberApplicationRepository.findByUserId(currentUser.getId(), paging);

        if (pagedVTuberApplications.hasContent()) {
            return pagedVTuberApplications.getContent().stream()
                    .map(this::mapToResponse)
                    .collect(Collectors.toList());
        }
        return List.of();
    }

    private String getSortDirection(String sortDir) {
        if (sortDir != null && sortDir.equalsIgnoreCase("asc")) {
            return "asc";
        }
        return "desc";
    }

    @Override
    @Transactional
    public String reviewVTuberApplication(long vTuberApplicationId, String status, String reason) {

        String token = jwtService.getCurrentToken(httpServletRequest);

        String tokenUsername = jwtService.getUsernameFromToken(token);

        Optional<SystemAccount> tokenSystemAccount = systemAccountRepository.findByUsername(tokenUsername);
        if (tokenSystemAccount.isEmpty()) {
            throw new CustomAuthenticationException("Authentication failed");
        }

        Optional<VTuberApplication> vTuberApplication = vTuberApplicationRepository.findById(vTuberApplicationId);
        if (vTuberApplication.isEmpty()) {
            throw new NotFoundException("VTuber Application not found");
        }

        vTuberApplication.get().setStatus(status);
        vTuberApplication.get().setReason(reason);
        vTuberApplication.get().setReviewAt(Instant.now());
        vTuberApplication.get().setReviewBy(tokenSystemAccount.get());
        vTuberApplicationRepository.save(vTuberApplication.get());

        if(status.equals("ACCEPTED")) {
            Optional<User> user = userRepository.findById(vTuberApplication.get().getUser().getId());
            if(user.isEmpty()) {
                throw new NotFoundException("User not found");
            }
            user.get().setRole("VTUBER");
            userRepository.save(user.get());
            
            // Send SSE notification to user about application approval
            // Also persists notification to database
            Long userId = user.get().getId();
            notificationService.sendVtuberApplicationNotification(userId, status, reason);
            log.info("Sent SSE notification to user {} for VTuber application approval", userId);
            
            return "Application accepted";
        } else {
            // Send SSE notification to user about application rejection
            // Also persists notification to database
            Long userId = vTuberApplication.get().getUser().getId();
            notificationService.sendVtuberApplicationNotification(userId, status, reason);
            log.info("Sent SSE notification to user {} for VTuber application rejection", userId);
            
            return "Application rejected";
        }
    }

    @Async
    public void validateChannelId(VTuberApplication vTuberApplication) throws ExecutionException, InterruptedException {

        String channelUrl = vTuberApplication.getChannelLink();

        String channelId = vTuberApplication.getChannelId();

        CompletableFuture<String> fetchedChannelId = youtubeAPIService.getChannelIdByUrl(channelUrl);

        if(Objects.equals(channelId, fetchedChannelId.get())){
            vTuberApplication.setIsMatchChannelLinkAndId(true);
            vTuberApplicationRepository.save(vTuberApplication);
        } else {
            vTuberApplication.setIsMatchChannelLinkAndId(false);
            vTuberApplicationRepository.save(vTuberApplication);
        }

    }

    private VTuberApplicationResponse mapToResponse(VTuberApplication entity) {
        VTuberApplicationResponse response = new VTuberApplicationResponse();

        response.setId(entity.getId());
        response.setChannelName(entity.getChannelName());
        response.setChannelLink(entity.getChannelLink());
        response.setChannelId(entity.getChannelId());
        response.setIsMatchChannelLinkAndId(entity.getIsMatchChannelLinkAndId());
        response.setStatus(entity.getStatus());
        response.setReason(entity.getReason());
        response.setCreatedAt(entity.getCreatedAt());
        response.setReviewAt(entity.getReviewAt());

        if (entity.getUser() != null) {
            response.setUserId(entity.getUser().getId());
            response.setUsername(entity.getUser().getUsername());
        }

        if (entity.getReviewBy() != null) {
            response.setReviewerId(entity.getReviewBy().getId());
            response.setReviewerUsername(entity.getReviewBy().getUsername());
        }

        return response;
    }
}
