package com.vnguyenx.realtimechatai.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Entity
@Table(name = "conversation_members")
@Data
public class ConversationMember {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "conversation_id", nullable = false)
    private Conversation conversation;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    private String nickname;

    @Column(name = "unread_count", nullable = false)
    private Integer unreadCount = 0;

    @Column(name = "joined_at")
    private LocalDateTime joinedAt = LocalDateTime.now();

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MemberRole role = MemberRole.MEMBER;

}