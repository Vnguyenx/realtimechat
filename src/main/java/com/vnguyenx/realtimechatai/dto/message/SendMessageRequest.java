package com.vnguyenx.realtimechatai.dto.message;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class SendMessageRequest {

    @NotBlank(message = "Nội dung tin nhắn không được để trống")
    private String content;

    @Pattern(regexp = "^(TEXT|IMAGE)$", message = "messageType chỉ được là TEXT hoặc IMAGE")
    private String messageType = "TEXT"; // mặc định TEXT nếu client không gửi field này
}