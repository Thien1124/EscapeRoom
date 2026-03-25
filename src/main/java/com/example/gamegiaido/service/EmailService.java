package com.example.gamegiaido.service;

import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    private final JavaMailSender mailSender;

    public EmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    public void sendOtpEmail(String to, String otpCode) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(to);
        message.setSubject("Mã xác thực OTP đăng ký Gamegiaido");
        message.setText("Xin chào,\n\nMã xác thực OTP của bạn là: " + otpCode + 
                        "\nMã này có hiệu lực trong 5 phút.\n\nTrân trọng.");
        mailSender.send(message);
    }
}
