package com.vnguyenx.realtimechatai.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.UUID;

@Service
public class SupabaseStorageService {

    @Value("${supabase.url}")
    private String supabaseUrl;

    @Value("${supabase.service-role-key}")
    private String serviceRoleKey;

    @Value("${supabase.storage.bucket.avatars}")
    private String avatarsBucket;

    @Value("${supabase.storage.bucket.chat-images}")
    private String chatImagesBucket;

    private final RestClient restClient = RestClient.create();

    public String uploadAvatar(MultipartFile file) {
        return uploadFile(file, avatarsBucket);
    }

    public String uploadChatImage(MultipartFile file) {
        return uploadFile(file, chatImagesBucket);
    }

    // Logic upload DÙNG CHUNG cho mọi loại ảnh, chỉ khác bucket đích
    public String uploadFile(MultipartFile file, String bucket) {
        // 1. Tạo tên file DUY NHẤT — tránh 2 user upload trùng tên file đè lên nhau
        String extension = getExtension(file.getOriginalFilename());
        String fileName = UUID.randomUUID() + extension;

        // 2. Xây dựng URL API upload của Supabase Storage
        //    Cấu trúc chuẩn: {supabaseUrl}/storage/v1/object/{bucket}/{tên file}
        String uploadUrl = supabaseUrl + "/storage/v1/object/" + avatarsBucket + "/" + fileName;

        try {
            // 3. Gọi POST request, gửi file dưới dạng byte[] thô trong body
            restClient.post()
                    .uri(uploadUrl)
                    .header("Authorization", "Bearer " + serviceRoleKey)
                    .header("apikey", serviceRoleKey)
                    .contentType(MediaType.parseMediaType(file.getContentType()))
                    .body(file.getBytes())
                    .retrieve()
                    .toBodilessEntity(); // không cần đọc response body, chỉ cần biết upload thành công (không lỗi)

        } catch (IOException e) {
            throw new IllegalArgumentException("Lỗi đọc file: " + e.getMessage());
        }

        // 4. Vì bucket là Public, URL truy cập ảnh có cấu trúc cố định — tự ghép, không cần gọi thêm API
        return supabaseUrl + "/storage/v1/object/public/" + avatarsBucket + "/" + fileName;
    }

    private String getExtension(String originalFilename) {
        if (originalFilename == null || !originalFilename.contains(".")) {
            return "";
        }
        return originalFilename.substring(originalFilename.lastIndexOf("."));
    }
}