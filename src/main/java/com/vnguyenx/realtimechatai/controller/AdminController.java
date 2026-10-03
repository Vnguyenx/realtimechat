package com.vnguyenx.realtimechatai.controller;

import com.vnguyenx.realtimechatai.dto.admin.*;
import com.vnguyenx.realtimechatai.service.AdminService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

    private final AdminService adminService;

    public AdminController(AdminService adminService) {
        this.adminService = adminService;
    }

    @GetMapping("/users")
    public ResponseEntity<Page<AdminUserSummaryResponse>> getUsers(
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Pageable pageable = PageRequest.of(page, size);
        return ResponseEntity.ok(adminService.getUsers(keyword, pageable));
    }

    @GetMapping("/users/{id}")
    public ResponseEntity<AdminUserDetailResponse> getUserDetail(@PathVariable Long id) {
        return ResponseEntity.ok(adminService.getUserDetail(id));
    }

    @PatchMapping("/users/{id}/ban")
    public ResponseEntity<AdminUserDetailResponse> banUser(@PathVariable Long id) {
        return ResponseEntity.ok(adminService.banUser(id));
    }

    @PatchMapping("/users/{id}/unban")
    public ResponseEntity<AdminUserDetailResponse> unbanUser(@PathVariable Long id) {
        return ResponseEntity.ok(adminService.unbanUser(id));
    }

    @GetMapping("/stats")
    public ResponseEntity<AdminStatsResponse> getStats() {
        return ResponseEntity.ok(adminService.getStats());
    }
}