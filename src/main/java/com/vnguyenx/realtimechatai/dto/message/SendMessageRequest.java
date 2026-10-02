package com.vnguyenx.realtimechatai.dto.message;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class SendMessageRequest {

    @NotBlank(message = "Nội dung tin nhắn không được để trống")
    private String content;

    @Pattern(regexp = "^(TEXT|IMAGE|STICKER)$", message = "messageType chỉ được là TEXT, IMAGE hoặc STICKER")
    private String messageType = "TEXT";

    @Pattern(regexp = "^(LOCAL|KLIPY)$", message = "stickerSource chỉ được là LOCAL hoặc KLIPY")
    private String stickerSource = "LOCAL"; // chỉ có ý nghĩa khi messageType = STICKER
}