package com.vnguyenx.realtimechatai.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

/**
 * Gửi email đặt lại mật khẩu qua API HTTPS của Resend (cổng 443), không dùng SMTP.
 * Không phụ thuộc Jackson nên chạy với mọi phiên bản Spring Boot.
 */
@Service
public class EmailService {

    @Value("${app.mail.from}")
    private String fromEmail;

    @Value("${app.frontend-url:http://localhost:5173}")
    private String frontendUrl;

    @Value("${app.resend.api-key}")
    private String resendApiKey;

    @Value("${app.resend.api-url:https://api.resend.com/emails}")
    private String resendApiUrl;

    private final HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(5))
            .build();

    public void sendPasswordResetEmail(String toEmail, String token) {
        String link = frontendUrl + "/reset-password?token=" + token;
        String text = "Bấm vào liên kết để đặt mật khẩu mới:\n" + link
                + "\n\nNếu liên kết không mở được, hãy nhập mã này vào trang đặt lại mật khẩu: " + token
                + "\n\nLiên kết và mã có hiệu lực 15 phút. Nếu không phải bạn yêu cầu, hãy bỏ qua email này.";

        String body = "{"
                + "\"from\":" + jsonString(fromEmail)
                + ",\"to\":[" + jsonString(toEmail) + "]"
                + ",\"subject\":" + jsonString("Đặt lại mật khẩu - RealtimeChatAI")
                + ",\"text\":" + jsonString(text)
                + "}";

        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(resendApiUrl))
                    .timeout(Duration.ofSeconds(10))
                    .header("Authorization", "Bearer " + resendApiKey)
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(body))
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() / 100 != 2) {
                throw new IllegalStateException("Resend trả về " + response.statusCode() + ": " + response.body());
            }
        } catch (IOException e) {
            throw new IllegalStateException("Không gọi được Resend: " + e.getMessage(), e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Gửi email bị gián đoạn", e);
        }
    }

    /** Đưa chuỗi về dạng chuỗi JSON hợp lệ (có dấu nháy kép bao ngoài). */
    private static String jsonString(String value) {
        StringBuilder sb = new StringBuilder(value.length() + 16).append('"');
        for (int i = 0; i < value.length(); i++) {
            char c = value.charAt(i);
            switch (c) {
                case '"' -> sb.append("\\\"");
                case '\\' -> sb.append("\\\\");
                case '\n' -> sb.append("\\n");
                case '\r' -> sb.append("\\r");
                case '\t' -> sb.append("\\t");
                default -> {
                    if (c < 0x20) {
                        sb.append(String.format("\\u%04x", (int) c));
                    } else {
                        sb.append(c);
                    }
                }
            }
        }
        return sb.append('"').toString();
    }
}