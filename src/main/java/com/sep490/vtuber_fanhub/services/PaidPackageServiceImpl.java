package com.sep490.vtuber_fanhub.services;

import com.sep490.vtuber_fanhub.dto.requests.CreatePaidPackageRequest;
import com.sep490.vtuber_fanhub.dto.requests.UpdatePaidPackageRequest;
import com.sep490.vtuber_fanhub.dto.responses.PaidPackageResponse;
import com.sep490.vtuber_fanhub.exceptions.NotFoundException;
import com.sep490.vtuber_fanhub.models.PaidPackage;
import com.sep490.vtuber_fanhub.repositories.PaidPackageRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PaidPackageServiceImpl implements PaidPackageService {

    private final PaidPackageRepository paidPackageRepository;

    @Override
    public List<PaidPackageResponse> getAllPaidPackages() {
        return paidPackageRepository.findAll().stream()
                .filter(p -> p.getIsActive() != null && p.getIsActive())
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public String createPaidPackage(CreatePaidPackageRequest request) {
        PaidPackage paidPackage = new PaidPackage();
        paidPackage.setPackageName(request.getPackageName());
        paidPackage.setPrice(request.getPrice());
        paidPackage.setPaidPoints(request.getPaidPoints());
        paidPackage.setDescription(request.getDescription());
        paidPackage.setIsActive(true);
        paidPackage.setCreatedAt(Instant.now());

        paidPackageRepository.save(paidPackage);
        return "Created paid package successfully";
    }

    @Override
    @Transactional
    public String updatePaidPackage(Long id, UpdatePaidPackageRequest request) {
        PaidPackage paidPackage = paidPackageRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Paid package not found"));

        if (request.getPackageName() != null) {
            paidPackage.setPackageName(request.getPackageName());
        }
        if (request.getPrice() != null) {
            paidPackage.setPrice(request.getPrice());
        }
        if (request.getPaidPoints() != null) {
            paidPackage.setPaidPoints(request.getPaidPoints());
        }
        if (request.getDescription() != null) {
            paidPackage.setDescription(request.getDescription());
        }

        paidPackageRepository.save(paidPackage);
        return "Updated paid package successfully";
    }

    @Override
    @Transactional
    public String deletePaidPackage(Long id) {
        PaidPackage paidPackage = paidPackageRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Paid package not found"));

        paidPackage.setIsActive(false);
        paidPackageRepository.save(paidPackage);
        return "Deleted paid package successfully";
    }

    private PaidPackageResponse mapToResponse(PaidPackage paidPackage) {
        PaidPackageResponse response = new PaidPackageResponse();
        response.setId(paidPackage.getId());
        response.setPackageName(paidPackage.getPackageName());
        response.setPrice(paidPackage.getPrice());
        response.setPaidPoints(paidPackage.getPaidPoints());
        response.setDescription(paidPackage.getDescription());
        return response;
    }
}
