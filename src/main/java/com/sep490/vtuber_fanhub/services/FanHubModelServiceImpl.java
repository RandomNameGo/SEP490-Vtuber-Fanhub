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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
    public FanHubModelResponse uploadModel(Long fanHubId, String name, MultipartFile modelFile,
                                          List<MultipartFile> spriteFiles, List<String> spriteNames,
                                          List<Integer> spriteFrames) throws IOException {
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

        // Handle multiple sprites
        if (spriteFiles != null && !spriteFiles.isEmpty()) {
            // Validation: Ensure metadata matches file count
            if (spriteNames == null || spriteNames.size() != spriteFiles.size() ||
                spriteFrames == null || spriteFrames.size() != spriteFiles.size()) {
                throw new IllegalArgumentException("The number of sprite names and frames must match the number of sprite files.");
            }

            Map<String, Object> spritesMap = model.getSprites();
            if (spritesMap == null) {
                spritesMap = new HashMap<>();
            }

            for (int i = 0; i < spriteFiles.size(); i++) {
                MultipartFile file = spriteFiles.get(i);
                if (file != null && !file.isEmpty()) {
                    String spriteUrl = cloudinaryService.uploadFile(file);
                    
                    String sName = spriteNames.get(i);
                    Integer sFrames = spriteFrames.get(i);

                    Map<String, Object> spriteData = new HashMap<>();
                    spriteData.put("url", spriteUrl);
                    spriteData.put("totalFrames", sFrames);
                    spriteData.put("name", sName);

                    spritesMap.put(sName, spriteData);
                }
            }
            model.setSprites(spritesMap);
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
                .sprites(model.getSprites())
                .createdAt(model.getCreatedAt())
                .build();
    }
}
