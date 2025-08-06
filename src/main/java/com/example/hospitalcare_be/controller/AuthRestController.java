package com.example.hospitalcare_be.controller;

import com.example.hospitalcare_be.dto.AccountRequest;
import com.example.hospitalcare_be.dto.AuthResponse;
import com.example.hospitalcare_be.dto.LoginRequest;
import com.example.hospitalcare_be.service.auth.IAuthService;
import jakarta.servlet.http.HttpServletResponse;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@AllArgsConstructor
@Slf4j
public class AuthRestController {

    private final IAuthService authService;

    @PostMapping("/register")
    public ResponseEntity<?> register(@ModelAttribute AccountRequest accountRequest) {
        try {
            return ResponseEntity.ok(authService.register(accountRequest));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }


    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest loginRequest) {
        AuthResponse authResponse = authService.login(loginRequest);
        try {
            return ResponseEntity.ok(authResponse);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Đăng nhap thất bại: " + e.getMessage());
        }
    }
    @PostMapping("/login-google")
    public ResponseEntity<?> loginWithGoogle(@RequestBody Map<String, String> body) {
        String token = body.get("token");
        AuthResponse authResponse = authService.loginWithGoogle(token);
        return ResponseEntity.ok(authResponse);
    }

    @PostMapping("/login-otp")
    public ResponseEntity<String> loginWithOtp(@RequestBody LoginRequest loginRequest) {
        try {
            authService.loginWithOtp(loginRequest);
            return ResponseEntity.ok("OTP sent successfully");
        } catch (RuntimeException ex) {
            log.error("Lỗi khi đăng nhập bằng OTP: {}", ex.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ex.getMessage());
        }
    }

    @PostMapping("/resend-otp")
    public ResponseEntity<?> resendOtp(@RequestBody LoginRequest loginRequest) {
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
    public ResponseEntity<?> verifyOtp(@RequestBody LoginRequest loginRequest) {
        AuthResponse authResponse = authService.verifyOtp(loginRequest);
        return ResponseEntity.ok(authResponse);
    }

    @GetMapping("/logout")
    public ResponseEntity<String> logout() {
        authService.logout();
        return ResponseEntity.ok("Logged out successfully");
    }
}
