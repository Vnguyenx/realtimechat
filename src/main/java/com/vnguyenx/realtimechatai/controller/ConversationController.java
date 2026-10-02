package com.vnguyenx.realtimechatai.controller;

import com.vnguyenx.realtimechatai.dto.conversation.ConversationResponse;
import com.vnguyenx.realtimechatai.dto.conversation.ConversationSummaryResponse;
import com.vnguyenx.realtimechatai.dto.conversation.UpdateNicknameRequest;
import com.vnguyenx.realtimechatai.service.ConversationService;

import jakarta.validation.Valid;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/conversations")
public class ConversationController {

    private final ConversationService conversationService;

    public ConversationController(ConversationService conversationService) {
        this.conversationService = conversationService;
    }

    @PostMapping("/with/{targetUsername}")
    public ResponseEntity<ConversationResponse> getOrCreateConversation(
            Authentication authentication,
            @PathVariable String targetUsername) {
        String username = authentication.getName();
        ConversationResponse response = conversationService.getOrCreateConversation(username, targetUsername);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    public ResponseEntity<List<ConversationSummaryResponse>> getMyConversations(Authentication authentication) {
        String username = authentication.getName();
        return ResponseEntity.ok(conversationService.getMyConversations(username));
    }

@PutMapping("/{conversationId}/nickname")
public ResponseEntity<ConversationResponse> updateNickname(
        Authentication authentication,
        @PathVariable Long conversationId,
        @Valid @RequestBody UpdateNicknameRequest request) {
    String username = authentication.getName();
    return ResponseEntity.ok(conversationService.updateNickname(username, conversationId, request));
}

}