package com.vnguyenx.realtimechatai.dto.user;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
public class UserProfileResponse {
    private Long id;
    private String username;
    private String email;
    private String fullName;
    private String avatarUrl;
    private LocalDate birthday;
    private String friendInviteCode;
    private LocalDateTime createdAt;
}