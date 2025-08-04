package com.example.hospitalcare_be.controller;

import com.example.hospitalcare_be.dto.AccountRequest;
import com.example.hospitalcare_be.dto.AuthResponse;
import com.example.hospitalcare_be.service.auth.IAuthService;
import lombok.AllArgsConstructor;
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
}
