package com.vnguyenx.realtimechatai.dto.admin;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
public class AdminUserDetailResponse {
    private Long id;
    private String username;
    private String email;
    private String fullName;
    private String avatarUrl;
    private LocalDate birthday;
    private String role;
    private Boolean isBanned;
    private Boolean isActive;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}