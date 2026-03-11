package com.sep490.vtuber_fanhub.services;

import com.sep490.vtuber_fanhub.dto.requests.CreatePostCommentRequest;
import com.sep490.vtuber_fanhub.models.PostComment;

public interface PostCommentService {

    boolean createPostComment(CreatePostCommentRequest createPostCommentRequest);

}
