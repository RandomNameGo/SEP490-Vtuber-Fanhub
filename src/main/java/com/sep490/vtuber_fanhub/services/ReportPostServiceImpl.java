package com.sep490.vtuber_fanhub.services;

import com.sep490.vtuber_fanhub.dto.requests.CreateReportPostRequest;
import com.sep490.vtuber_fanhub.exceptions.NotFoundException;
import com.sep490.vtuber_fanhub.models.Post;
import com.sep490.vtuber_fanhub.models.ReportPost;
import com.sep490.vtuber_fanhub.models.User;
import com.sep490.vtuber_fanhub.repositories.PostRepository;
import com.sep490.vtuber_fanhub.repositories.ReportPostRepository;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ReportPostServiceImpl implements ReportPostService {

    private final ReportPostRepository reportPostRepository;

    private final AuthService authService;

    private final HttpServletRequest httpServletRequest;

    private final PostRepository postRepository;

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
}
