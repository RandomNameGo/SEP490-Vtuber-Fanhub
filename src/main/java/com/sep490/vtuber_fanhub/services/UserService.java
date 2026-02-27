package com.sep490.vtuber_fanhub.services;

import com.sep490.vtuber_fanhub.dto.requests.CreateUserRequest;
import com.sep490.vtuber_fanhub.dto.requests.UpdateUserRequest;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

public interface UserService {

    String createUser(CreateUserRequest createUserRequest);

    String updateUser(UpdateUserRequest updateUserRequest);

    String uploadAvatarFrame(MultipartFile avatarFile, MultipartFile frameFile) throws IOException;
}
