package com.vnguyenx.realtimechatai.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    private final JavaMailSender mailSender;

    
    @Value("${app.mail.from}") private String fromEmail;
    @Value("${app.frontend-url:http://localhost:5173}") private String frontendUrl;

    public EmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    public void sendPasswordResetEmail(String toEmail, String token) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(fromEmail);
        message.setTo(toEmail);
        message.setSubject("Đặt lại mật khẩu - RealtimeChatAI");
        String link = frontendUrl + "/reset-password?token=" + token;
        message.setFrom(fromEmail);
        message.setText("Bấm vào liên kết để đặt mật khẩu mới:\n" + link +
    "\n\nLiên kết có hiệu lực 15 phút. Nếu không phải bạn yêu cầu, hãy bỏ qua email này.");

        mailSender.send(message);
    }
}