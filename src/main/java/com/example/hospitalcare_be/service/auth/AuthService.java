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
import com.example.hospitalcare_be.util.OtpCache;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService implements IAuthService {

    private final AuthenticationManager authenticationManager;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final IAccountRepository accountRepo;
    private final IRoleRepository roleRepo;
    private final CloudinaryService cloudinaryService;
    private final OtpService otpService;
    private final OtpCache otpCache;
    private final EmailService emailService;

    @Override
    public AuthResponse register(AccountRequest request) {
        if ((request.getEmail() == null || request.getEmail().isBlank()) &&
                (request.getPhonenumber() == null || request.getPhonenumber().isBlank())) {
            throw new RuntimeException("Email hoặc số điện thoại không được để trống.");
        }

        if (request.getEmail() != null && accountRepo.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Email đã tồn tại trong hệ thống ");
        }

        if (request.getPhonenumber() != null && accountRepo.existsByPhonenumber(request.getPhonenumber())) {
            throw new RuntimeException("Số điện thoại đã tồn tại trong hệ thống ");
        }

        Account account = new Account();
        account.setFullname(request.getFullname());
        account.setEmail(request.getEmail());
        account.setPhonenumber(request.getPhonenumber());
        account.setPassword(passwordEncoder.encode(request.getPassword()));
        account.setGender(request.getGender());
        account.setStatus("PENDING");

        if (request.getAvatarFile() != null && !request.getAvatarFile().isEmpty()) {
            Map uploadResult = cloudinaryService.uploadImage(request.getAvatarFile(), "avatars");
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


    @Value("${GOOGLE_CLIENT_ID}")
    private String googleClientId;
    @Override
    public AuthResponse loginWithGoogle(String token) {
        try {
            // Bước 1: Verify Google Token
            GoogleIdTokenVerifier verifier = new GoogleIdTokenVerifier.Builder(
                    new NetHttpTransport(),
                    JacksonFactory.getDefaultInstance())
                    .setAudience(Collections.singletonList(googleClientId))
                    .build();

            GoogleIdToken idToken = verifier.verify(token);

            if (idToken == null) {
                throw new RuntimeException("Invalid Google Token");
            }

            GoogleIdToken.Payload payload = idToken.getPayload();

            String email = payload.getEmail();
            String name = (String) payload.get("name");
            String picture = (String) payload.get("picture");

            // Bước 2: Kiểm tra user đã tồn tại chưa
            Account account = accountRepo.findByEmail(email).orElseGet(() -> {
                // Bước 3: Tạo user mới nếu chưa có
                Account newUser = new Account();
                newUser.setEmail(email);
                newUser.setFullname(name);
                newUser.setAvtPath(picture);
                newUser.setPassword("");
                newUser.setPhonenumber("");
                newUser.setRoles(Set.of(roleRepo.findByName("ROLE_PATIENT")
                        .orElseThrow(() -> new RuntimeException("Role USER not found"))));
                return accountRepo.save(newUser);
            });

            // Bước 4: Sinh JWT
            String jwt = jwtUtil.generateToken(account.getEmail(),
                    account.getRoles().stream().map(Role::getName).toList());

            // Bước 5: Trả AuthResponse
            return AuthResponse.builder()
                    .token(jwt)
                    .fullname(account.getFullname())
                    .email(account.getEmail())
                    .phonenumber(account.getPhonenumber())
                    .avatarUrl(account.getAvtPath())
                    .roles(account.getRoles().stream().map(Role::getName).collect(Collectors.toSet()))
                    .build();

        } catch (Exception e) {
            throw new RuntimeException("Đăng nhập Google thất bại: " + e.getMessage());
        }
    }



    @Override
    public void loginWithOtp(LoginRequest loginRequest) {
        String identifier = loginRequest.getEmailOrPhone();
        log.info("Đăng nhập bằng OTP với identifier: {}", identifier);

        Account account = otpService.findAccountByIdentifier(identifier);
        if (account == null) {
            log.warn("Tài khoản không tồn tại với identifier: {}", identifier);
            throw new RuntimeException("Tài khoản không tồn tại trong hệ thống.");
        }

        if (identifier.contains("@")) {
            try {
                otpService.generateAndSendOtp(identifier);
                log.info("Đã gửi OTP tới email: {}", identifier);
            } catch (Exception e) {
                log.error("Lỗi khi gửi OTP tới email: {}", identifier, e);
                throw new RuntimeException("Không thể gửi OTP. Vui lòng thử lại sau.");
            }
        } else {
            log.warn("Identifier không hợp lệ (không phải email): {}", identifier);
            throw new RuntimeException("Email không hợp lệ.");
        }
    }


    @Override
    public void resendOtp(LoginRequest loginRequest) {
        if (loginRequest.getEmailOrPhone() == null || loginRequest.getEmailOrPhone().isBlank()) {
            throw new RuntimeException("Email or phone must not be empty.");
        }

        Account account = otpService.findAccountByIdentifier(loginRequest.getEmailOrPhone());
        if (account == null) {
            throw new RuntimeException("Account not found with email or phone: " + loginRequest.getEmailOrPhone());
        }

        String key = account.getId().toString();
        String oldOtp = otpCache.get(key);

        if (oldOtp != null) {
            throw new RuntimeException("OTP is still valid. Please wait before requesting a new one.");
        }

        String newOtp = otpService.generateOtp();
        otpCache.put(key, newOtp, 180);
        emailService.sendOtpEmail(account.getEmail(), newOtp);

        System.out.println("[INFO] Resent OTP to: " + account.getEmail() + " with code: " + newOtp);
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

    @Override
    public void logout() {

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


