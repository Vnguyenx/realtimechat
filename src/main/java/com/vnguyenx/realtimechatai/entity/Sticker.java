package com.vnguyenx.realtimechatai.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Entity
@Table(name = "stickers")
@Data
public class Sticker {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(name = "image_url", nullable = false, unique = true)
    private String imageUrl;

    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();
}