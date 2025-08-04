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

import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AuthService implements IAuthService {

    private final AuthenticationManager authenticationManager;
    private  final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final IAccountRepository accountRepo;
    private final IRoleRepository roleRepo;
    private final CloudinaryService cloudinaryService;


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
        if (request.getAvatarFile() != null && !request.getAvatarFile().isEmpty()) {
            Map uploadResult = cloudinaryService.uploadImage(request.getAvatarFile());
            String imageUrl = (String) uploadResult.get("secure_url");
            account.setAvtPath(imageUrl);
        } else {
            account.setAvtPath("https://res.cloudinary.com/dk6vu2mlh/image/upload/v1754320302/x7hglgokkpmvsvjqm8xz.jpg");
        }


        Role selectedRole = roleRepo.findByName(request.getRole())
                .orElseThrow(() -> new RuntimeException("Role not found"));
        account.setRoles(Set.of(selectedRole));

        accountRepo.save(account);

        String loginIdentifier = request.getPhonenumber() != null
                ? request.getPhonenumber()
                : request.getEmail();

        Authentication auth = performAuthentication(loginIdentifier, request.getPassword());
        String token = jwtUtil.generateToken(auth);

        return buildAuthResponse(account, token);
    }

    @Override
    public AuthResponse login(LoginRequest loginRequest) {
        return null;
    }
    private Authentication performAuthentication(String emailOrPhone, String rawPassword) {
        return authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(emailOrPhone, rawPassword)
        );
    }

    private AuthResponse buildAuthResponse(Account account, String token) {
        return new AuthResponse(
                token,
                account.getFullname(),
                account.getEmail(),
                account.getPhonenumber(),
                account.getAvtPath(),
                account.getRoles().stream()
                        .map(Role::getName)
                        .collect(Collectors.toSet())
        );
    }
}
