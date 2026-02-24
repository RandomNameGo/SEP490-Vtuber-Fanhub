package com.sep490.vtuber_fanhub.services;

import com.sep490.vtuber_fanhub.dto.requests.CreateUserRequest;

public interface UserService {

    String createUser(CreateUserRequest createUserRequest);

}
