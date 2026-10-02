package com.vnguyenx.realtimechatai.repository;

import com.vnguyenx.realtimechatai.entity.Sticker;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StickerRepository extends JpaRepository<Sticker, Long> {
    boolean existsByImageUrl(String imageUrl);
}