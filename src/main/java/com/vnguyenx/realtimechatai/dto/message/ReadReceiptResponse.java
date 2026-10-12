package com.vnguyenx.realtimechatai.dto.message;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ReadReceiptResponse {
    private Long conversationId;
    private Long userId;
    private String username;
    private Long lastReadMessageId; // null nếu người này chưa đọc gì
}