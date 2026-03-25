package com.sep490.vtuber_fanhub.services;

import com.sep490.vtuber_fanhub.dto.requests.CreateReportPostRequest;
import com.sep490.vtuber_fanhub.dto.responses.ReportPostResponse;
import com.sep490.vtuber_fanhub.exceptions.NotFoundException;
import com.sep490.vtuber_fanhub.models.FanHub;
import com.sep490.vtuber_fanhub.models.Post;
import com.sep490.vtuber_fanhub.models.ReportPost;
import com.sep490.vtuber_fanhub.models.User;
import com.sep490.vtuber_fanhub.repositories.FanHubMemberRepository;
import com.sep490.vtuber_fanhub.repositories.FanHubRepository;
import com.sep490.vtuber_fanhub.repositories.PostRepository;
import com.sep490.vtuber_fanhub.repositories.ReportPostRepository;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ReportPostServiceImpl implements ReportPostService {

    private final ReportPostRepository reportPostRepository;

    private final AuthService authService;

    private final HttpServletRequest httpServletRequest;

    private final PostRepository postRepository;

    private final FanHubRepository fanHubRepository;

    private final FanHubMemberRepository fanHubMemberRepository;

    @Override
    public String createReportPost(CreateReportPostRequest createReportPostRequest) {

        User currentUser = authService.getUserFromToken(httpServletRequest);

        Optional<Post> post = postRepository.findById(createReportPostRequest.getPostId());
        if (post.isEmpty()) {
            throw new NotFoundException("Post not found");
        }

        ReportPost reportPost = new ReportPost();
        reportPost.setPost(post.get());
        reportPost.setReportedBy(currentUser);
        reportPost.setReason(createReportPostRequest.getReason());
        reportPost.setStatus("PENDING");
        reportPost.setCreatedAt(Instant.now());
        reportPostRepository.save(reportPost);

        return "Report post sent successfully";
    }

    @Override
    public List<ReportPostResponse> getReportPostsByFanHubId(Long fanHubId, int pageNo, int pageSize, String sortBy) {
        User currentUser = authService.getUserFromToken(httpServletRequest);

        Optional<FanHub> fanHub = fanHubRepository.findById(fanHubId);
        if (fanHub.isEmpty()) {
            throw new NotFoundException("FanHub not found");
        }

        // Check if user is VTUBER and owns this FanHub
        boolean isOwner = "VTUBER".equals(currentUser.getRole()) &&
                fanHub.get().getOwnerUser().getId().equals(currentUser.getId());

        // Check if user is a member with MODERATOR role
        boolean isModerator = fanHubMemberRepository.findByHubIdAndUserId(fanHubId, currentUser.getId())
                .map(member -> "MODERATOR".equals(member.getRoleInHub()))
                .orElse(false);

        if (!isOwner && !isModerator) {
            throw new AccessDeniedException("Access denied");
        }

        PageRequest pageRequest = PageRequest.of(pageNo, pageSize, Sort.by(Sort.Direction.DESC, sortBy));
        Page<ReportPost> reportPostPage = reportPostRepository.findByFanHubId(fanHubId, pageRequest);

        return reportPostPage.getContent().stream()
                .map(this::mapToReportPostResponse)
                .collect(Collectors.toList());
    }

    private ReportPostResponse mapToReportPostResponse(ReportPost reportPost) {
        ReportPostResponse response = new ReportPostResponse();
        response.setReportId(reportPost.getId());
        response.setPostId(reportPost.getPost().getId());
        response.setFanHubId(reportPost.getPost().getHub().getId());
        response.setPostTitle(reportPost.getPost().getTitle());
        response.setReportedByUserId(reportPost.getReportedBy().getId());
        response.setReportedByUsername(reportPost.getReportedBy().getUsername());
        response.setReportedByDisplayName(reportPost.getReportedBy().getDisplayName());
        response.setReason(reportPost.getReason());
        response.setStatus(reportPost.getStatus());
        response.setCreatedAt(reportPost.getCreatedAt());
        return response;
    }
}
