package com.sep490.vtuber_fanhub.services;

import com.sep490.vtuber_fanhub.dto.responses.PaidPackageResponse;
import com.sep490.vtuber_fanhub.models.PaidPackage;
import com.sep490.vtuber_fanhub.repositories.PaidPackageRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

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
