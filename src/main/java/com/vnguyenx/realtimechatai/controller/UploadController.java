package com.vnguyenx.realtimechatai.controller;

import com.vnguyenx.realtimechatai.dto.upload.UploadResponse;
import com.vnguyenx.realtimechatai.service.SupabaseStorageService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/upload")
public class UploadController {

    private static final List<String> ALLOWED_TYPES = List.of("image/jpeg", "image/png", "image/webp");
    private static final long AVATAR_MAX_SIZE = 5 * 1024 * 1024; // 5MB, khớp với cấu hình bucket
    private static final long CHAT_IMAGE_MAX_SIZE = 10 * 1024 * 1024; // 10MB — ảnh chat cho phép lớn hơn avatar


    private final SupabaseStorageService storageService;

    public UploadController(SupabaseStorageService storageService) {
        this.storageService = storageService;
    }

   @PostMapping("/avatar")
    public ResponseEntity<UploadResponse> uploadAvatar(@RequestParam("file") MultipartFile file) {
        validateFile(file, AVATAR_MAX_SIZE);
        String url = storageService.uploadAvatar(file);
        return ResponseEntity.ok(new UploadResponse(url));
    }

    @PostMapping("/chat-image")
    public ResponseEntity<UploadResponse> uploadChatImage(@RequestParam("file") MultipartFile file) {
        validateFile(file, CHAT_IMAGE_MAX_SIZE);
        String url = storageService.uploadChatImage(file);
        return ResponseEntity.ok(new UploadResponse(url));
    }

    // Logic validate DÙNG CHUNG, chỉ khác giới hạn size truyền vào
    private void validateFile(MultipartFile file, long maxSize) {
        if (file.isEmpty()) {
            throw new IllegalArgumentException("File không được để trống");
        }
        if (!ALLOWED_TYPES.contains(file.getContentType())) {
            throw new IllegalArgumentException("Chỉ chấp nhận file ảnh JPEG, PNG hoặc WEBP");
        }
        if (file.getSize() > maxSize) {
            throw new IllegalArgumentException("File vượt quá giới hạn cho phép");
        }
    }
}