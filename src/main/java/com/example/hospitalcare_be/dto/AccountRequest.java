package com.example.hospitalcare_be.dto;

import lombok.Data;
import org.springframework.web.multipart.MultipartFile;
import java.util.Set;


@Data
public class AccountRequest {
    private String fullname;
    private String phonenumber;
    private String email;
    private String password;
    private String gender;
    private MultipartFile avatarFile;
    private Set<String> roles;
}
