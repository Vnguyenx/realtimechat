package com.vnguyenx.realtimechatai.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Entity
@Table(name = "conversations")
@Data
public class Conversation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_a_id", nullable = false)
    private User userA; // luôn là user có id NHỎ HƠN

    @ManyToOne
    @JoinColumn(name = "user_b_id", nullable = false)
    private User userB; // luôn là user có id LỚN HƠN

    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "last_message")
    private String lastMessage;

    @Column(name = "last_message_at")
    private LocalDateTime lastMessageAt;

    @Column(name = "unread_count_a", nullable = false)
    private Integer unreadCountA = 0;

    @Column(name = "unread_count_b", nullable = false)
    private Integer unreadCountB = 0;

}