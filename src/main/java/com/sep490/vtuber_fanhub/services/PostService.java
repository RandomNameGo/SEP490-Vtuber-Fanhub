package com.sep490.vtuber_fanhub.services;

import com.sep490.vtuber_fanhub.dto.requests.CreatePostRequest;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface PostService {

    String createPost(CreatePostRequest request, List<MultipartFile> images, MultipartFile video);
}
