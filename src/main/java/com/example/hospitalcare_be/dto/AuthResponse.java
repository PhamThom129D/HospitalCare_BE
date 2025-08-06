package com.example.hospitalcare_be.dto;

import com.cloudinary.AccessControlRule;
import com.example.hospitalcare_be.model.Role;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class AuthResponse {
    private String token;
    private String fullname;
    private String email;
    private String phonenumber;
    private String avatarUrl;
    private Set<String> roles;
}
