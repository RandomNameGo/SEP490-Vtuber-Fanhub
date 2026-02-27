package com.sep490.vtuber_fanhub.dto.requests;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Getter
@Setter
public class CreatePostRequest {

    @NotNull(message = "FanHub ID must not be null")
    private Long fanHubId;

    @NotBlank(message = "Post type must not be blank")
    @Pattern(regexp = "^(TEXT|IMAGE|VIDEO)$", message = "Post type must be TEXT, IMAGE, or VIDEO")
    private String postType;

    @NotBlank(message = "Title must not be blank")
    @Size(max = 255, message = "Title must not exceed 255 characters")
    private String title;

//    @NotBlank(message = "Content must not be blank")
//    @Size(max = 5000, message = "Content must not exceed 5000 characters")
    private String content;

    @Size(max = 5, message = "Cannot exceed 10 hashtags")
    private List<String> hashtags;
}
