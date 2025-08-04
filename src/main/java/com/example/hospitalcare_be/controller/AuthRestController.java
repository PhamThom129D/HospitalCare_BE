package com.example.hospitalcare_be.controller;

import com.example.hospitalcare_be.dto.AccountRequest;
import com.example.hospitalcare_be.dto.AuthResponse;
import com.example.hospitalcare_be.dto.LoginRequest;
import com.example.hospitalcare_be.service.auth.IAuthService;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@AllArgsConstructor
public class AuthRestController {

    private final IAuthService authService;

    @PostMapping("/register")
    public ResponseEntity<?> register(@ModelAttribute AccountRequest accountRequest) {
        return ResponseEntity.ok( authService.register(accountRequest));
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@ModelAttribute LoginRequest loginRequest) {
        AuthResponse authResponse = authService.login(loginRequest);
        return ResponseEntity.ok(authResponse);
    }
    @PostMapping("/login-gmail")
    public ResponseEntity<?> loginWithGoogle(@ModelAttribute LoginRequest loginRequest) {
        AuthResponse authResponse = authService.loginWithGoogle(loginRequest);
        return ResponseEntity.ok(authResponse);
    }
    @PostMapping("/login-otp")
    public ResponseEntity<String> loginWithOtp(@ModelAttribute LoginRequest loginRequest) {
        authService.loginWithOtp(loginRequest);
        return ResponseEntity.ok("OTP sent successfully");
    }
    @PostMapping("/resend-otp")
    public ResponseEntity<?> resendOtp(@ModelAttribute LoginRequest loginRequest) {
        try {
            authService.resendOtp(loginRequest);
            return ResponseEntity.ok("OTP resent successfully");
        } catch (RuntimeException e) {
            System.err.println("[ERROR] Resend OTP failed: " + e.getMessage());
            return ResponseEntity.badRequest().body("Failed to resend OTP: " + e.getMessage());
        } catch (Exception e) {
            System.err.println("[UNEXPECTED ERROR] " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Unexpected error occurred.");
        }
    }

    @PostMapping("/verify-otp")
    public ResponseEntity<?> verifyOtp(@ModelAttribute LoginRequest loginRequest) {
        AuthResponse authResponse = authService.verifyOtp(loginRequest);
        return ResponseEntity.ok(authResponse);
    }
}
