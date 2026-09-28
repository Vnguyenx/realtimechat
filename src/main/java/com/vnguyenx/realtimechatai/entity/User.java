package com.vnguyenx.realtimechatai.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;
import java.time.LocalDate;

@Entity
@Table(name = "users")
@Data
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String username; //để dùng cho việc đăng nhập và sử dụng chức năng kết bạn

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String password;

    @Column(name = "full_name")
    private String fullName; //hiển thị tên đầy đủ của người dùng, có thể null nếu người dùng chưa cập nhật

    @Column(name = "avatar_url")
    private String avatarUrl;

    @Column(name = "friend_invite_code", unique = true)
    private String friendInviteCode;

    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "updated_at")
    private LocalDateTime updatedAt = LocalDateTime.now();

    @Column(nullable = false)
    private String role = "CUSTOMER"; // hoặc dùng Enum để chặt chẽ hơn

    @Column(name = "is_banned", nullable = false)
    private Boolean isBanned = false;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;
    
    @Column(name = "birthday")
    private LocalDate birthday;

}