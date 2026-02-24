package com.sep490.vtuber_fanhub.services;

import com.sep490.vtuber_fanhub.dto.requests.CreateUserRequest;
import com.sep490.vtuber_fanhub.dto.responses.LoginResponse;

public interface AuthService {
    LoginResponse login(String username, String password);
}
