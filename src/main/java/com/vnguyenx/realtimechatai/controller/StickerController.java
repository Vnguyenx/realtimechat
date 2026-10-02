package com.vnguyenx.realtimechatai.controller;

import com.vnguyenx.realtimechatai.dto.sticker.StickerResponse;
import com.vnguyenx.realtimechatai.service.KlipyService;
import com.vnguyenx.realtimechatai.service.StickerService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/stickers")
public class StickerController {

    private final StickerService stickerService;
    private final KlipyService klipyService;

    public StickerController(StickerService stickerService, KlipyService klipyService) {
        this.stickerService = stickerService;
        this.klipyService = klipyService;
    }

    @GetMapping
    public ResponseEntity<List<StickerResponse>> getLocalStickers() {
        return ResponseEntity.ok(stickerService.getAllStickers());
    }

    @GetMapping("/search")
    public ResponseEntity<List<Map<String, Object>>> searchStickers(
        Authentication authentication,
        @RequestParam String q,
        @RequestParam(defaultValue = "1") int page) {
    return ResponseEntity.ok(klipyService.searchStickers(q, page, authentication.getName()));
    }
}