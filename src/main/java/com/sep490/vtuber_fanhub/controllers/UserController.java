package com.sep490.vtuber_fanhub.controllers;

import com.sep490.vtuber_fanhub.dto.requests.CreateUserRequest;
import com.sep490.vtuber_fanhub.dto.responses.APIResponse;
import com.sep490.vtuber_fanhub.services.EmailService;
import com.sep490.vtuber_fanhub.services.OtpService;
import com.sep490.vtuber_fanhub.services.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RestController
@RequestMapping("vhub/api/v1/user")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    private final EmailService emailService;

    private final OtpService otpService;

    @PostMapping("/verify")
    public ResponseEntity<?> verify(@RequestParam("email") String email){
        String otp = otpService.generateOtp(email);
        emailService.sendOtpEmail(email, otp);
        return ResponseEntity.ok().body(APIResponse.<String>builder()
                .success(true)
                .message("Success")
                .data("OTP has been sent to email. Please verify to complete registration.")
                .build()
        );
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody @Valid CreateUserRequest request) {
        if(otpService.verifyOtp(request.getEmail(), request.getOtp())){
            return ResponseEntity.ok().body(APIResponse.<String>builder()
                    .success(true)
                    .message("Success")
                    .data(userService.createUser(request))
                    .build()
            );
        }
        return ResponseEntity.ok().body(APIResponse.<String>builder()
                .success(true)
                .message("Fail")
                .data("Can not register user")
                .build()
        );
    }

    @PostMapping("/upload-avatar-frame")
    public ResponseEntity<?> uploadAvatarFrame(@RequestParam(value = "avatar", required = false) MultipartFile avatarFile,
                                               @RequestParam(value = "frame", required = false) MultipartFile frameFile) throws IOException {
        return ResponseEntity.ok().body(APIResponse.<String>builder()
                .success(true)
                .message("Success")
                .data(userService.uploadAvatarFrame(avatarFile, frameFile))
                .build()
        );
    }

}
