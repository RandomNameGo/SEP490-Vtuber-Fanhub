package com.sep490.vtuber_fanhub.services;

import com.sep490.vtuber_fanhub.dto.responses.PaidPackageResponse;
import java.util.List;

public interface PaidPackageService {
    List<PaidPackageResponse> getAllPaidPackages();
}
