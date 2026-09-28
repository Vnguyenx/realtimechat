package com.vnguyenx.realtimechatai.controller;

import com.vnguyenx.realtimechatai.dto.message.MessageResponse;
import com.vnguyenx.realtimechatai.dto.message.SendMessageRequest;
import com.vnguyenx.realtimechatai.service.MessageService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/conversations/{conversationId}/messages")
public class MessageController {

    private final MessageService messageService;

    public MessageController(MessageService messageService) {
        this.messageService = messageService;
    }

    @PostMapping
    public ResponseEntity<MessageResponse> sendMessage(
            Authentication authentication,
            @PathVariable Long conversationId,
            @Valid @RequestBody SendMessageRequest request) {
        String username = authentication.getName();
        MessageResponse response = messageService.sendMessage(username, conversationId, request);
        return ResponseEntity.status(201).body(response);
    }

    @GetMapping
    public ResponseEntity<Page<MessageResponse>> getMessages(
            Authentication authentication,
            @PathVariable Long conversationId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "6") int size) {
        String username = authentication.getName();
        Pageable pageable = PageRequest.of(page, size);
        return ResponseEntity.ok(messageService.getMessages(username, conversationId, pageable));
    }

    @PostMapping("/read")
    public ResponseEntity<Void> markAsRead(
            Authentication authentication,
            @PathVariable Long conversationId) {
        String username = authentication.getName();
        messageService.markAsRead(username, conversationId);
        return ResponseEntity.ok().build();
    }
}