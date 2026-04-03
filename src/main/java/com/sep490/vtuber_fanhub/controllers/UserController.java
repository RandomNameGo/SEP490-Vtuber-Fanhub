package com.sep490.vtuber_fanhub.controllers;

import com.sep490.vtuber_fanhub.dto.requests.CreateUserRequest;
import com.sep490.vtuber_fanhub.dto.requests.SelectUserBadgeRequest;
import com.sep490.vtuber_fanhub.dto.requests.SetOshiRequest;
import com.sep490.vtuber_fanhub.dto.requests.UpdateUserRequest;
import com.sep490.vtuber_fanhub.dto.responses.APIResponse;
import com.sep490.vtuber_fanhub.dto.responses.UserDetailResponse;
import com.sep490.vtuber_fanhub.dto.responses.UserResponse;
import com.sep490.vtuber_fanhub.services.EmailService;
import com.sep490.vtuber_fanhub.services.OtpService;
import com.sep490.vtuber_fanhub.services.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

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
                .data(otp)
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

    @PutMapping("/update")
    public ResponseEntity<?> updateUser(@RequestBody @Valid UpdateUserRequest request) {
        return ResponseEntity.ok().body(APIResponse.<String>builder()
                .success(true)
                .message("Success")
                .data(userService.updateUser(request))
                .build()
        );
    }

    @GetMapping("/{userId}")
    public ResponseEntity<?> getUserDetailWithBadge(@PathVariable Long userId) {
        return ResponseEntity.ok().body(APIResponse.<UserResponse>builder()
                .success(true)
                .message("Success")
                .data(userService.getUserDetailWithBadge(userId))
                .build()
        );
    }

    @GetMapping("/{userId}/badges")
    public ResponseEntity<?> getAllUserBadges(@PathVariable Long userId) {
        List<UserResponse.UserAllBadgeResponse> badges = userService.getAllUserBadges(userId);
        return ResponseEntity.ok().body(APIResponse.<List<UserResponse.UserAllBadgeResponse>>builder()
                .success(true)
                .message("Success")
                .data(badges)
                .build()
        );
    }

    @PostMapping("/badges/select-display")
    public ResponseEntity<?> selectDisplayBadges(@RequestBody SelectUserBadgeRequest request) {
        return ResponseEntity.ok().body(APIResponse.<String>builder()
                .success(true)
                .message("Success")
                .data(userService.updateUserBadgeDisplay(request))
                .build()
        );
    }

    @GetMapping("/user-name/{userName}")
    public ResponseEntity<?> getUserDetailWithBadgeByUserName(@PathVariable String userName) {
        return ResponseEntity.ok().body(APIResponse.<UserResponse>builder()
                .success(true)
                .message("Success")
                .data(userService.getUserDetailWithBadgeByUserName(userName))
                .build()
        );
    }

    @PutMapping("/set-oshi")
    public ResponseEntity<?> setOshi(@RequestBody @Valid SetOshiRequest request) {
        return ResponseEntity.ok().body(APIResponse.<String>builder()
                .success(true)
                .message("Success")
                .data(userService.setOshi(request))
                .build()
        );
    }

    @GetMapping("/me")
    public ResponseEntity<?> getCurrentUserDetail() {
        return ResponseEntity.ok().body(APIResponse.<UserDetailResponse>builder()
                .success(true)
                .message("Success")
                .data(userService.getCurrentUserDetail())
                .build()
        );
    }

}
