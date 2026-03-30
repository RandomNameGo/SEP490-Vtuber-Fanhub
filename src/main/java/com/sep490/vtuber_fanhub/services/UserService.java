package com.sep490.vtuber_fanhub.services;

import com.sep490.vtuber_fanhub.dto.requests.CreateUserRequest;
import com.sep490.vtuber_fanhub.dto.requests.SelectUserBadgeRequest;
import com.sep490.vtuber_fanhub.dto.requests.SetOshiRequest;
import com.sep490.vtuber_fanhub.dto.requests.UpdateUserRequest;
import com.sep490.vtuber_fanhub.dto.responses.UserResponse;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

public interface UserService {

    String createUser(CreateUserRequest createUserRequest);

    String updateUser(UpdateUserRequest updateUserRequest);

    String uploadAvatarFrame(MultipartFile avatarFile, MultipartFile frameFile) throws IOException;

    UserResponse getUserDetailWithBadge(Long userId);

    UserResponse getUserDetailWithBadgeByUserName(String userName);

    List<UserResponse.UserAllBadgeResponse> getAllUserBadges(Long userId);

    String updateUserBadgeDisplay(SelectUserBadgeRequest request);

    String setOshi(SetOshiRequest request);
}
