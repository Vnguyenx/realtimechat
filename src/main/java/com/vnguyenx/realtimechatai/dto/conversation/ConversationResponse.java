package com.vnguyenx.realtimechatai.dto.conversation;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
public class ConversationResponse {
    private Long conversationId;
    private Long friendUserId;   // thông tin NGƯỜI KIA — giống hệt cơ chế toResponse() ở Friendship
    private String friendUsername;
    private String friendFullName;
    private String friendAvatarUrl;
    private LocalDateTime createdAt;
    private String lastMessage;
    private LocalDateTime lastMessageAt;
}