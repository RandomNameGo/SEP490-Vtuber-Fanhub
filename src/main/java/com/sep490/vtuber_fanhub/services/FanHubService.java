package com.sep490.vtuber_fanhub.services;

import com.sep490.vtuber_fanhub.dto.requests.CreateFanHubRequest;
import com.sep490.vtuber_fanhub.dto.responses.FanHubResponse;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

public interface FanHubService {

    String createFanHub(CreateFanHubRequest request);

    String uploadFanHubBannerBackGroundAvatar(long fanHubId, MultipartFile banner, List<MultipartFile> highlight, MultipartFile avatar) throws IOException;

    List<FanHubResponse> getAllFanHubs(int pageNo, int pageSize, String sortBy, boolean includePrivate);

    List<FanHubResponse> getTopFanHubs(int pageNo, int pageSize, String category);

    FanHubResponse getFanHubBySubdomain(String subdomain);
}
