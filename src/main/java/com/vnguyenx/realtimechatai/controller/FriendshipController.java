package com.vnguyenx.realtimechatai.controller;

import com.vnguyenx.realtimechatai.dto.friend.FriendRequest;
import com.vnguyenx.realtimechatai.dto.friend.FriendResponse;
import com.vnguyenx.realtimechatai.service.FriendshipService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/friends")
public class FriendshipController {

    private final FriendshipService friendshipService;

    public FriendshipController(FriendshipService friendshipService) {
        this.friendshipService = friendshipService;
    }

    @PostMapping("/request")
    public ResponseEntity<FriendResponse> sendRequest(
            Authentication authentication,
            @Valid @RequestBody FriendRequest request) {
        String username = authentication.getName();
        FriendResponse response = friendshipService.sendRequest(username, request);
        return ResponseEntity.status(201).body(response);
    }

    @PostMapping("/{friendshipId}/accept")
    public ResponseEntity<FriendResponse> acceptRequest(
            Authentication authentication,
            @PathVariable Long friendshipId) {
        String username = authentication.getName();
        FriendResponse response = friendshipService.acceptRequest(username, friendshipId);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{friendshipId}/reject")
    public ResponseEntity<FriendResponse> rejectRequest(
            Authentication authentication,
            @PathVariable Long friendshipId) {
        String username = authentication.getName();
        FriendResponse response = friendshipService.rejectRequest(username, friendshipId);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    public ResponseEntity<List<FriendResponse>> getFriendList(Authentication authentication) {
        String username = authentication.getName();
        return ResponseEntity.ok(friendshipService.getFriendList(username));
    }

    @GetMapping("/received-requests")
    public ResponseEntity<List<FriendResponse>> getPendingRequests(Authentication authentication) {
        String username = authentication.getName();
        return ResponseEntity.ok(friendshipService.getPendingRequests(username));
    }

    @GetMapping("/sent-requests")
    public ResponseEntity<List<FriendResponse>> getSentRequests(Authentication authentication) {
        String username = authentication.getName();
        return ResponseEntity.ok(friendshipService.getSentRequests(username));
    }

    @DeleteMapping("/{friendshipId}")
    public ResponseEntity<Void> cancelRequest(
        Authentication authentication,
        @PathVariable Long friendshipId) {
        String username = authentication.getName();
        friendshipService.cancelRequest(username, friendshipId);
        return ResponseEntity.noContent().build();
}

}