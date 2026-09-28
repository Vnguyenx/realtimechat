package com.vnguyenx.realtimechatai.controller;

import com.vnguyenx.realtimechatai.dto.conversation.ConversationResponse;
import com.vnguyenx.realtimechatai.service.ConversationService;
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
    public ResponseEntity<List<ConversationResponse>> getMyConversations(Authentication authentication) {
        String username = authentication.getName();
        return ResponseEntity.ok(conversationService.getMyConversations(username));
    }
}