package com.example.hospitalcare_be.service.auth;

import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;

@Service
public class EmailService {
    private final JavaMailSender mailSender;

    public EmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    public void sendOtpEmail(String toEmail, String otpCode) {
        String subject = "Mã xác thực OTP từ HospitalCare";
        String body = """
            <p>Xin chào,</p>
            <p>Bạn đã yêu cầu mã xác thực OTP.</p>
            <p><strong>Mã OTP của bạn là: <span style="font-size:18px;">%s</span></strong></p>
            <p>Mã này sẽ hết hạn sau 5 phút.</p>
            <br>
            <p>Trân trọng,<br>Đội ngũ HospitalCare</p>
            """.formatted(otpCode);

        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setTo(toEmail);
            helper.setSubject(subject);
            helper.setText(body, true);
            mailSender.send(message);
        } catch (MessagingException e) {
            throw new RuntimeException("Gửi email thất bại: " + e.getMessage());
        }
    }
}
