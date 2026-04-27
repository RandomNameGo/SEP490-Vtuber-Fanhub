package com.sep490.vtuber_fanhub.services;

import com.sep490.vtuber_fanhub.dto.requests.CreateVTuberApplication;
import com.sep490.vtuber_fanhub.dto.responses.VTuberApplicationResponse;

import java.util.List;
import java.util.concurrent.ExecutionException;

public interface VTuberApplicationService {
    String createVTuberApplication(CreateVTuberApplication request) throws ExecutionException, InterruptedException;

    List<VTuberApplicationResponse> getAllVTuberApplications(int pageNo, int pageSize, String sortBy, String sortDir);

    List<VTuberApplicationResponse> getMyVTuberApplications(int pageNo, int pageSize, String sortBy, String sortDir);

    String reviewVTuberApplication (long vTuberApplicationId, String status, String reason);
}
