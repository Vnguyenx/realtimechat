package com.vnguyenx.realtimechatai.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    private final JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String fromEmail;

    public EmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    public void sendPasswordResetEmail(String toEmail, String token) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(fromEmail);
        message.setTo(toEmail);
        message.setSubject("Đặt lại mật khẩu - RealtimeChatAI");
        message.setText(
            "Bạn vừa yêu cầu đặt lại mật khẩu.\n\n" +
            "Mã token của bạn là: " + token + "\n\n" +
            "Mã này có hiệu lực trong 15 phút. Nếu không phải bạn yêu cầu, hãy bỏ qua email này."
        );

        mailSender.send(message);
    }
}