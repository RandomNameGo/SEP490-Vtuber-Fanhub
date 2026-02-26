package com.sep490.vtuber_fanhub.services;

import com.sep490.vtuber_fanhub.dto.requests.CreateUserRequest;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

public interface UserService {

    String createUser(CreateUserRequest createUserRequest);

    String uploadAvatarFrame(MultipartFile avatarFile, MultipartFile frameFile) throws IOException;
}
