package com.example.hospitalcare_be.service.auth;

import com.example.hospitalcare_be.dto.AccountRequest;
import com.example.hospitalcare_be.dto.AuthResponse;
import com.example.hospitalcare_be.dto.LoginRequest;

public interface IAuthService {
    AuthResponse register(AccountRequest accountRequest);
    AuthResponse login(LoginRequest loginRequest);
    AuthResponse loginWithGoogle(String token);
    void loginWithOtp(LoginRequest loginRequest);
    void resendOtp(LoginRequest loginRequest);
    AuthResponse verifyOtp(LoginRequest loginRequest);

    void logout();
}
