package com.vnguyenx.realtimechatai.dto.conversation;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
public class ConversationResponse {
    private Long conversationId;
    private Long friendUserId;
    private String friendUsername;
    private String friendFullName;
    private String friendAvatarUrl;
    private String friendNickname; // MỚI
    private String myNickname;     // MỚI
    private LocalDateTime createdAt;
}