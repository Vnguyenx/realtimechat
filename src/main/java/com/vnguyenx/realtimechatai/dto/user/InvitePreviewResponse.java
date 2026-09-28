package com.vnguyenx.realtimechatai.dto.user;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class InvitePreviewResponse {
    private String username;
    private String fullName;
    private String avatarUrl;
}