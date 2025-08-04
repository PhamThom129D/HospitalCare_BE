package com.example.hospitalcare_be.dto;

import lombok.Data;
import java.util.Set;

@Data
public class AccountResponse {
    private Long id;
    private String fullname;
    private String phonenumber;
    private String email;
    private String gender;
    private String avatarUrl;
    private Set<String> roles;
    private String status;
    private String createdAt;
    private String updatedAt;
}


