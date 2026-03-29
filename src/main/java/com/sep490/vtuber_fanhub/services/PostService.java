package com.sep490.vtuber_fanhub.services;

import com.sep490.vtuber_fanhub.dto.requests.CreatePollPostRequest;
import com.sep490.vtuber_fanhub.dto.requests.CreatePostRequest;
import com.sep490.vtuber_fanhub.dto.responses.SummarizePostResponse;
import com.sep490.vtuber_fanhub.dto.responses.PostResponse;
import com.sep490.vtuber_fanhub.dto.responses.TranslatePostResponse;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface PostService {

    String createPost(CreatePostRequest request, List<MultipartFile> images, MultipartFile video);

    String createPollPost(CreatePollPostRequest request);

    List<PostResponse> getPendingPosts(Long fanHubId, int pageNo, int pageSize, String sortBy);

    List<PostResponse> getPosts(Long fanHubId, int pageNo, int pageSize, String sortBy, String postHashtag);

    List<PostResponse> getAnnouncementAndEventPosts(Long fanHubId, int pageNo, int pageSize, String sortBy);

    String reviewPost(Long postId, String status);

    String sendAiValidate(Long postId);

    List<PostResponse> getPersonalizedFeed(int pageNo, int pageSize, String sortBy);

    TranslatePostResponse translatePost(Long postId);

    SummarizePostResponse summarizePost(Long postId);

    String likePost(Long postId);

    String unlikePost(Long postId);

    String votePost(Long postId, Long optionId);

    String unVotePost(Long postId);
}
