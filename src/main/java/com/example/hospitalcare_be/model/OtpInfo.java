package com.example.hospitalcare_be.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;


@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class OtpInfo {
    private String otpCode;
    private long expiryTime;

    public boolean isExpired() {
        return System.currentTimeMillis() > expiryTime;
    }
}
