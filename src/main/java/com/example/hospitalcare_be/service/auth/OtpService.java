package com.example.hospitalcare_be.service.auth;

import com.example.hospitalcare_be.model.Account;
import com.example.hospitalcare_be.repository.IAccountRepository;
import com.example.hospitalcare_be.util.OtpCache;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import java.util.Random;

@Service
@RequiredArgsConstructor

public class OtpService {

    private final OtpCache otpCache;
    private final EmailService mailService;
    private final IAccountRepository accountRepo;

    public void generateAndSendOtp(String emailOrPhone) {
        Account account = findAccountByIdentifier(emailOrPhone);
        String otp = generateOtp();
        otpCache.put(account.getId().toString(), otp, 180);
        mailService.sendOtpEmail(account.getEmail(), otp);
    }

    public boolean verifyOtp(Long accountId, String inputOtp) {
        String cachedOtp = otpCache.get(accountId.toString());
        if (cachedOtp != null && cachedOtp.equals(inputOtp)) {
            otpCache.remove(accountId.toString());
            return true;
        }
        return false;
    }

    String generateOtp() {
        return String.valueOf(new Random().nextInt(900000) + 100000);
    }

    Account findAccountByIdentifier(String identifier) {
        return identifier.contains("@")
                ? accountRepo.findByEmail(identifier)
                .orElseThrow(() -> new RuntimeException("Account not found with email: " + identifier))
                : accountRepo.findByPhonenumber(identifier)
                .orElseThrow(() -> new RuntimeException("Account not found with phone: " + identifier));
    }
}
