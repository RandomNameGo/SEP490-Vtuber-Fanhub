package com.sep490.vtuber_fanhub.services;


import com.sep490.vtuber_fanhub.models.OtpVerification;
import com.sep490.vtuber_fanhub.repositories.OtpVerificationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Random;

@Service
@RequiredArgsConstructor
public class OtpService {
    private final OtpVerificationRepository otpVerificationRepository;

    public String generateOtp(String email) {
        String otp = String.valueOf(new Random().nextInt(999999));
        OtpVerification otpVerification = new OtpVerification();
        otpVerification.setEmail(email);
        otpVerification.setOtpCode(otp);
        otpVerification.setExpiresAt((Instant.now().plus(5, ChronoUnit.MINUTES)));
        otpVerificationRepository.save(otpVerification);
        return otp;
    }

    public boolean verifyOtp(String email, String otp) {
        OtpVerification record = otpVerificationRepository.findTopByEmailOrderByExpiresAtDesc(email);

        if (record != null
                && record.getOtpCode().equals(otp)
                && Instant.now().isBefore(record.getExpiresAt())) {
            record.setIsUsed(true);
            return true;
        }

        return false;
    }
}
