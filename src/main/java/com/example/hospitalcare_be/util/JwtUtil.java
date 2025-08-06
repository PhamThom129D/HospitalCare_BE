package com.example.hospitalcare_be.util;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class JwtUtil {

    @Value("${jwt.secret}")
    private String secretKey;

    @Value("${jwt.expiration}")
    private long expirationMs;

    private Key key;

    @PostConstruct
    public void init() {
        if (secretKey == null || secretKey.length() < 32) {
            System.err.println("⚠️ JWT secret key is too short or not set properly. Using fallback key (NOT SECURE in production).");
            secretKey = "DefaultSecretKeyThatIsLongEnough1234567890";
        }
        this.key = Keys.hmacShaKeyFor(secretKey.getBytes());
    }


    public String generateToken(Authentication authentication) {
        String username = authentication.getName();
        Collection<? extends GrantedAuthority> authorities = authentication.getAuthorities();

        List<String> roles = authorities.stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toList());

        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + expirationMs);

        return Jwts.builder()
                .setSubject(username)
                .claim("roles", roles) // 🟢 Nhúng role vào token
                .setIssuedAt(now)
                .setExpiration(expiryDate)
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    // Extract username
    public String getUsernameFromToken(String token) {
        return parseToken(token).getBody().getSubject();
    }

    // Validate token
    public boolean isValidToken(String token) {
        try {
            parseToken(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }

    private Jws<Claims> parseToken(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token);
    }
    public String generateToken(String email, List<String> roles) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + expirationMs);

        return Jwts.builder()
                .setSubject(email)
                .claim("roles", roles)
                .setIssuedAt(now)
                .setExpiration(expiryDate)
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

}
//JwtUtil là class tiện ích (utility) để:
//
//Tạo JWT khi người dùng đăng nhập thành công.
//
//Giải mã JWT để lấy username.
//
//Xác minh JWT xem có hợp lệ không (chữ ký đúng, chưa hết hạn, đúng định dạng...).
