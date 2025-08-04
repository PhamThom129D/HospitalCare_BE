package com.example.hospitalcare_be.dto;


import lombok.Data;

@Data
public class LoginRequest {
        private String emailOrPhone;
        private String password;
        private String otpCode;
        private String googleToken;
}
