package com.sep490.vtuber_fanhub.services;

import com.sep490.vtuber_fanhub.dto.responses.FanHubModelResponse;
import com.sep490.vtuber_fanhub.exceptions.CustomAuthenticationException;
import com.sep490.vtuber_fanhub.exceptions.NotFoundException;
import com.sep490.vtuber_fanhub.models.FanHub;
import com.sep490.vtuber_fanhub.models.FanHubModel;
import com.sep490.vtuber_fanhub.models.User;
import com.sep490.vtuber_fanhub.repositories.FanHubModelRepository;
import com.sep490.vtuber_fanhub.repositories.FanHubRepository;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.Instant;

@Service
@RequiredArgsConstructor
public class FanHubModelServiceImpl implements FanHubModelService {

    private final FanHubModelRepository fanHubModelRepository;
    private final FanHubRepository fanHubRepository;
    private final CloudinaryService cloudinaryService;
    private final AuthService authService;
    private final HttpServletRequest httpServletRequest;

    @Override
    @Transactional
    public FanHubModelResponse uploadModel(Long fanHubId, String name, MultipartFile modelFile, MultipartFile spriteFile) throws IOException {
        User currentUser = authService.getUserFromToken(httpServletRequest);

        FanHub fanHub = fanHubRepository.findByIdAndIsActive(fanHubId, true)
                .orElseThrow(() -> new NotFoundException("FanHub not found or is inactive"));

        if (!fanHub.getOwnerUser().getId().equals(currentUser.getId())) {
            throw new CustomAuthenticationException("Access denied. Only the owner can upload a model for this FanHub.");
        }

        FanHubModel model = fanHubModelRepository.findById(fanHubId).orElse(new FanHubModel());
        model.setId(fanHubId);
        model.setName(name);

        if (modelFile != null && !modelFile.isEmpty()) {
            String modelUrl = cloudinaryService.uploadFile(modelFile);
            model.setFileUrl(modelUrl);
        }

        if (spriteFile != null && !spriteFile.isEmpty()) {
            String spriteUrl = cloudinaryService.uploadFile(spriteFile);
            model.setSpriteUrl(spriteUrl);
        }

        if (model.getCreatedAt() == null) {
            model.setCreatedAt(Instant.now());
        }

        FanHubModel savedModel = fanHubModelRepository.save(model);
        return mapToFanHubModelResponse(savedModel);
    }

    @Override
    @Transactional(readOnly = true)
    public FanHubModelResponse getModelByFanHubId(Long fanHubId) {
        FanHubModel model = fanHubModelRepository.findById(fanHubId)
                .orElseThrow(() -> new NotFoundException("FanHub model not found for ID: " + fanHubId));
        return mapToFanHubModelResponse(model);
    }

    private FanHubModelResponse mapToFanHubModelResponse(FanHubModel model) {
        return FanHubModelResponse.builder()
                .id(model.getId())
                .name(model.getName())
                .fileUrl(model.getFileUrl())
                .spriteUrl(model.getSpriteUrl())
                .createdAt(model.getCreatedAt())
                .build();
    }
}
