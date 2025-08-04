package com.example.hospitalcare_be.service.auth;

import com.example.hospitalcare_be.dto.OtpVerification;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.LocalDateTime;

@Service
public class OtpService {
    private final HttpSession session;
    private final SecureRandom random = new SecureRandom();

    public OtpService(HttpSession session) {
        this.session = session;
    }

    public OtpVerification generateOtp(Long accountId) {
        String otpCode = String.format("%06d", random.nextInt(1_000_000));
        LocalDateTime expiresAt = LocalDateTime.now().plusMinutes(3);

        OtpVerification otp = new OtpVerification(accountId, otpCode,LocalDateTime.now(), expiresAt,true);
        otp.setCreatedAt(LocalDateTime.now());

        session.setAttribute("OTP_" + accountId, otp);
        return otp;
    }

    public boolean verifyOtp(Long accountId, String code) {
        OtpVerification otp = (OtpVerification) session.getAttribute("OTP_" + accountId);
        if (otp == null) return false;

        boolean notExpired = LocalDateTime.now().isBefore(otp.getExpiresAt());
        boolean match = otp.getOtpCode().equals(code) && notExpired;

        if (match) {
            session.removeAttribute("OTP_" + accountId);
        }

        return match;
    }
}
