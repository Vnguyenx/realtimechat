package com.vnguyenx.realtimechatai.dto.conversation;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
public class ConversationSummaryResponse {
    private Long conversationId;
    private String type;          // "DIRECT" | "GROUP"
    private String displayName;   // DIRECT: nickname→fullName→username của bạn; GROUP: tên nhóm
    private String avatarUrl;     // DIRECT: avatar bạn; GROUP: null (chưa làm avatar nhóm ở Tier 2)
    private Integer memberCount;  // null với DIRECT, số thành viên với GROUP
    private String lastMessage;
    private LocalDateTime lastMessageAt;
    private Integer unreadCount;
    private LocalDateTime createdAt;
}