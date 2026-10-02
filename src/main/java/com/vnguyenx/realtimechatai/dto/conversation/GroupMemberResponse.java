package com.vnguyenx.realtimechatai.dto.conversation;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class GroupMemberResponse {
    private Long userId;
    private String username;
    private String fullName;
    private String avatarUrl;
    private String nickname;
    private String role;
}