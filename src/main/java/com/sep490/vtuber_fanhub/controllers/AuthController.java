package com.sep490.vtuber_fanhub.controllers;

import com.sep490.vtuber_fanhub.dto.responses.APIResponse;
import com.sep490.vtuber_fanhub.dto.responses.LoginResponse;
import com.sep490.vtuber_fanhub.services.AuthService;
import com.sep490.vtuber_fanhub.services.RefreshTokenService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("vhub/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    private final RefreshTokenService refreshTokenService;

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestParam("username") String username, @RequestParam("password") String password) {
        return ResponseEntity.ok().body(APIResponse.<LoginResponse>builder()
                .success(true)
                .message("Success")
                .data(authService.login(username, password))
                .build()
        );
    }

    @PostMapping("/refresh-token")
    public ResponseEntity<?> refreshToken(@RequestParam("refresh-token") String token) {
        return ResponseEntity.ok().body(APIResponse.<LoginResponse>builder()
                .success(true)
                .message("Success")
                .data(refreshTokenService.createNewToken(token))
                .build()
        );
    }
}
