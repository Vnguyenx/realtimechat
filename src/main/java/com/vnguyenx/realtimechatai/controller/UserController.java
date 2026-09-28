package com.vnguyenx.realtimechatai.controller;

import com.vnguyenx.realtimechatai.dto.user.InvitePreviewResponse;
import com.vnguyenx.realtimechatai.dto.user.UpdateAvatarRequest;
import com.vnguyenx.realtimechatai.dto.user.UpdateProfileRequest;
import com.vnguyenx.realtimechatai.dto.user.UserProfileResponse;
import com.vnguyenx.realtimechatai.dto.user.UserSearchResponse;
import com.vnguyenx.realtimechatai.service.UserService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Size;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@Validated
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/me")
    public ResponseEntity<UserProfileResponse> getCurrentUser(Authentication authentication) {
        String username = authentication.getName();
        return ResponseEntity.ok(userService.getCurrentUserProfile(username));
    }

    @PutMapping("/me")
    public ResponseEntity<UserProfileResponse> updateProfile(
            Authentication authentication,
            @Valid @RequestBody UpdateProfileRequest request) {
        String username = authentication.getName();
        return ResponseEntity.ok(userService.updateProfile(username, request));
    }

    @PatchMapping("/me/avatar")
    public ResponseEntity<UserProfileResponse> updateAvatar(
            Authentication authentication,
            @Valid @RequestBody UpdateAvatarRequest request) {
        String username = authentication.getName();
        return ResponseEntity.ok(userService.updateAvatar(username, request));
    }

    @GetMapping("/search")
    public ResponseEntity<List<UserSearchResponse>> searchUsers(
            @RequestParam
            @Size(min = 2, max = 20, message = "Từ khoá tìm kiếm phải từ 2-20 ký tự")
            String keyword) {
        return ResponseEntity.ok(userService.searchUsers(keyword));
    }

    @GetMapping("/invite/{code}")
    public ResponseEntity<InvitePreviewResponse> getInvitePreview(@PathVariable String code) {
        return ResponseEntity.ok(userService.getInvitePreview(code));
    }
}
