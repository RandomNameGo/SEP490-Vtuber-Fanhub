package com.sep490.vtuber_fanhub.repositories;

import com.sep490.vtuber_fanhub.models.OtpVerification;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OtpVerificationRepository extends JpaRepository<OtpVerification, Long> {

    OtpVerification findTopByEmailOrderByExpiresAtDesc(String email);

}