package com.sep490.vtuber_fanhub.services;

import com.sep490.vtuber_fanhub.dto.requests.CreateFanHubRequest;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

public interface FanHubService {

    String createFanHub(CreateFanHubRequest request);

    String uploadFanHubBannerBackGroundAvatar(long fanHubId, MultipartFile banner, MultipartFile background ,MultipartFile avatar) throws IOException;

}
