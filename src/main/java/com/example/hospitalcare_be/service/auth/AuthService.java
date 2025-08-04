package com.example.hospitalcare_be.service.auth;

import com.example.hospitalcare_be.dto.AccountRequest;
import com.example.hospitalcare_be.dto.AuthResponse;
import com.example.hospitalcare_be.dto.LoginRequest;
import com.example.hospitalcare_be.model.Account;
import com.example.hospitalcare_be.model.Role;
import com.example.hospitalcare_be.repository.IAccountRepository;
import com.example.hospitalcare_be.repository.IRoleRepository;
import com.example.hospitalcare_be.service.CloudinaryService;
import com.example.hospitalcare_be.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AuthService implements IAuthService {

    private final AuthenticationManager authenticationManager;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final IAccountRepository accountRepo;
    private final IRoleRepository roleRepo;
    private final CloudinaryService cloudinaryService;
    private final OtpService otpService;
    private final EmailService emailService;

    @Override
    public AuthResponse register(AccountRequest request) {
        if ((request.getEmail() == null || request.getEmail().isBlank()) &&
                (request.getPhonenumber() == null || request.getPhonenumber().isBlank())) {
            throw new RuntimeException("Email or phone number is required");
        }

        if (request.getEmail() != null && accountRepo.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Email already exists");
        }

        if (request.getPhonenumber() != null && accountRepo.existsByPhonenumber(request.getPhonenumber())) {
            throw new RuntimeException("Phone number already exists");
        }

        Account account = new Account();
        account.setFullname(request.getFullname());
        account.setEmail(request.getEmail());
        account.setPhonenumber(request.getPhonenumber());
        account.setPassword(passwordEncoder.encode(request.getPassword()));
        account.setGender(request.getGender());
        account.setStatus("PENDING");

        if (request.getAvatarFile() != null && !request.getAvatarFile().isEmpty()) {
            Map uploadResult = cloudinaryService.uploadImage(request.getAvatarFile());
            account.setAvtPath((String) uploadResult.get("secure_url"));
        } else {
            account.setAvtPath("https://res.cloudinary.com/dk6vu2mlh/image/upload/v1754320302/x7hglgokkpmvsvjqm8xz.jpg");
        }

        Role selectedRole = roleRepo.findByName(request.getRole())
                .orElseThrow(() -> new RuntimeException("Role not found"));
        account.setRoles(Set.of(selectedRole));

        accountRepo.save(account);

        String identifier = request.getPhonenumber() != null ? request.getPhonenumber() : request.getEmail();
        Authentication auth = performAuthentication(identifier, request.getPassword());
        String token = jwtUtil.generateToken(auth);

        return buildAuthResponse(token, account);
    }

    @Override
    public AuthResponse login(LoginRequest request) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getEmailOrPhone(), request.getPassword()
                )
        );

        String token = jwtUtil.generateToken(authentication);
        Account account = otpService.findAccountByIdentifier(request.getEmailOrPhone());

        return buildAuthResponse(token, account);
    }

    @Override
    public AuthResponse loginWithGoogle(LoginRequest loginRequest) {
        return null;
    }

    @Override
    public String loginWithOtp(LoginRequest loginRequest) {
        Account account = otpService.findAccountByIdentifier(loginRequest.getEmailOrPhone());
        if (account == null) {
            throw new RuntimeException("Account not found with email or phone: " + loginRequest.getEmailOrPhone());
        }
        String otp = otpService.generateOtp();
        if (loginRequest.getEmailOrPhone().contains("@")) {
            otpService.generateAndSendOtp(loginRequest.getEmailOrPhone());
        } else {
            throw new RuntimeException("Invalid email.");
        }
        return otp;
    }

    @Override
    public AuthResponse verifyOtp(LoginRequest loginRequest) {
        Account account = otpService.findAccountByIdentifier(loginRequest.getEmailOrPhone());
        boolean success = otpService.verifyOtp(account.getId(),loginRequest.getOtpCode());
        if (!success) {
            throw new RuntimeException("Mã OTP không đúng.");
        }
        String token = jwtUtil.generateToken(account.getEmail(), account.getRoles().stream().map(Role::getName).toList());
        return buildAuthResponse(token, account);
    }

    private AuthResponse buildAuthResponse(String token, Account account) {
        return AuthResponse.builder()
                .token(token)
                .email(account.getEmail())
                .phonenumber(account.getPhonenumber())
                .fullname(account.getFullname())
                .avatarUrl(account.getAvtPath())
                .roles(account.getRoles().stream().map(Role::getName).collect(Collectors.toSet()))
                .build();
    }

    private Authentication performAuthentication(String identifier, String rawPassword) {
        return authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(identifier, rawPassword)
        );
    }

}


