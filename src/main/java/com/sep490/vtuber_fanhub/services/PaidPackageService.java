package com.sep490.vtuber_fanhub.services;

import com.sep490.vtuber_fanhub.dto.requests.CreatePaidPackageRequest;
import com.sep490.vtuber_fanhub.dto.requests.UpdatePaidPackageRequest;
import com.sep490.vtuber_fanhub.dto.responses.PaidPackageResponse;
import java.util.List;

public interface PaidPackageService {
    List<PaidPackageResponse> getAllPaidPackages();

    String createPaidPackage(CreatePaidPackageRequest request);

    String updatePaidPackage(Long id, UpdatePaidPackageRequest request);

    String deletePaidPackage(Long id);
}
