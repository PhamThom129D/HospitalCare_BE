package com.example.hospitalcare_be.security;

import com.example.hospitalcare_be.model.Account;
import com.example.hospitalcare_be.repository.IAccountRepository;
import lombok.AllArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.security.core.userdetails.User;

import java.util.List;

@Service
@AllArgsConstructor
public class UserDetailService implements UserDetailsService {
    private final IAccountRepository accountRepo;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Account account;
        boolean isEmail;

        if (username.contains("@")) {
            isEmail = true;
            account = accountRepo.findByEmail(username)
                    .orElseThrow(() -> new UsernameNotFoundException("Email not found: " + username));
        } else {
            isEmail = false;
            account = accountRepo.findByPhonenumber(username)
                    .orElseThrow(() -> new UsernameNotFoundException("Phone number not found: " + username));
        }

        String principal = isEmail ? account.getEmail() : account.getPhonenumber();

        List<SimpleGrantedAuthority> authorities = account.getRoles().stream()
                .map(role -> new SimpleGrantedAuthority(role.getName()))
                .toList();
        return new User(
                principal,
                account.getPassword(),
                authorities
        );
    }

}
