package com.vnguyenx.realtimechatai.dto.user;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class UserSearchResponse {
    private Long id;
    private String username;
    private String fullName;
    private String avatarUrl;
}