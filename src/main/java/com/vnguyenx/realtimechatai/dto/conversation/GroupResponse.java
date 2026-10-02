package com.vnguyenx.realtimechatai.dto.conversation;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
@AllArgsConstructor
public class GroupResponse {
    private Long conversationId;
    private String name;
    private List<GroupMemberResponse> members;
    private LocalDateTime createdAt;
}