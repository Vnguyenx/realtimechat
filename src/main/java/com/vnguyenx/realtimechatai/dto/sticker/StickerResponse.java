package com.vnguyenx.realtimechatai.dto.sticker;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class StickerResponse {
    private Long id;
    private String name;
    private String imageUrl;
}