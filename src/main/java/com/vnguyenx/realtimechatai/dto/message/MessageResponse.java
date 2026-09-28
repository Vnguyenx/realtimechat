package com.vnguyenx.realtimechatai.dto.message;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
public class MessageResponse {
    private Long id;
    private Long senderId;
    private String senderUsername;
    private String content;
    private String messageType;
    private Boolean isRead;
    private LocalDateTime createdAt;
}