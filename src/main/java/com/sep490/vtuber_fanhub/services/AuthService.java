package com.sep490.vtuber_fanhub.services;

import com.sep490.vtuber_fanhub.dto.requests.CreateUserRequest;
import com.sep490.vtuber_fanhub.dto.responses.LoginResponse;
import lombok.extern.java.Log;

public interface AuthService {
    LoginResponse login(String username, String password);

    LoginResponse SystemAccountLogin(String username, String password);
}
