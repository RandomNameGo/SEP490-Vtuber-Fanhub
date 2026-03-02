package com.sep490.vtuber_fanhub.services;

import com.sep490.vtuber_fanhub.dto.requests.CreatePostRequest;
import com.sep490.vtuber_fanhub.dto.responses.PostResponse;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface PostService {

    String createPost(CreatePostRequest request, List<MultipartFile> images, MultipartFile video);

    List<PostResponse> getPendingPosts(Long fanHubId, int pageNo, int pageSize, String sortBy);

    List<PostResponse> getPosts(Long fanHubId, int pageNo, int pageSize, String sortBy, String postHashtag);

    String reviewPost(Long postId, String status);
}
