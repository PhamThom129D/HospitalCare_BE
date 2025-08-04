package com.example.hospitalcare_be.dto;

import com.example.hospitalcare_be.model.Role;
import lombok.Data;

import java.util.Set;

@Data
public class AuthResponse {
    private String token;
    private String fullname;
    private String email;
    private String phonenumber;
    private String avatarUrl;
    private Set<String> roles;
}
