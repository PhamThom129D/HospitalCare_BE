package com.example.hospitalcare_be.dto;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class OtpVerification {
    private Long accountId;
    private String otpCode;
    private LocalDateTime createdAt;
    private LocalDateTime expiresAt;
    private boolean isVerified = false;
}
