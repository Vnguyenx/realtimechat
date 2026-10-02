package com.vnguyenx.realtimechatai.service;

import com.vnguyenx.realtimechatai.dto.sticker.StickerResponse;
import com.vnguyenx.realtimechatai.repository.StickerRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class StickerService {

    private final StickerRepository stickerRepository;

    public StickerService(StickerRepository stickerRepository) {
        this.stickerRepository = stickerRepository;
    }

    public List<StickerResponse> getAllStickers() {
        return stickerRepository.findAll().stream()
                .map(s -> new StickerResponse(s.getId(), s.getName(), s.getImageUrl()))
                .toList();
    }
}