package com.vnguyenx.realtimechatai.controller;

import com.vnguyenx.realtimechatai.dto.message.MessageResponse;
import com.vnguyenx.realtimechatai.dto.message.ReadReceiptResponse;
import com.vnguyenx.realtimechatai.dto.message.SendMessageRequest;
import com.vnguyenx.realtimechatai.service.MessageService;
import jakarta.validation.Valid;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/conversations/{conversationId}/messages")
public class MessageController {

    private final MessageService messageService;
    private final SimpMessagingTemplate messagingTemplate;


    public MessageController(MessageService messageService, SimpMessagingTemplate messagingTemplate) {
        this.messageService = messageService;
        this.messagingTemplate = messagingTemplate;
    }

    @PostMapping
    public ResponseEntity<MessageResponse> sendMessage(
            Authentication authentication,
            @PathVariable Long conversationId,
            @Valid @RequestBody SendMessageRequest request) {
        MessageResponse response = messageService.sendMessage(authentication.getName(), conversationId, request);
        return ResponseEntity.status(201).body(response);
    }

    @GetMapping
    public ResponseEntity<Page<MessageResponse>> getMessages(
            Authentication authentication,
            @PathVariable Long conversationId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "6") int size) {
        Pageable pageable = PageRequest.of(page, size);
        return ResponseEntity.ok(messageService.getMessages(authentication.getName(), conversationId, pageable));
    }

    @PostMapping("/read")
    public ResponseEntity<ReadReceiptResponse> markAsRead(
            Authentication authentication,
            @PathVariable Long conversationId) {
        ReadReceiptResponse receipt = messageService.markAsRead(authentication.getName(), conversationId);
        // Báo cho mọi người đang mở cuộc trò chuyện này: "người X vừa đọc tới tin số N"
        messagingTemplate.convertAndSend("/topic/conversation." + conversationId + ".read", receipt);
        return ResponseEntity.ok(receipt);
    }

    @GetMapping("/read-status")
    public ResponseEntity<List<ReadReceiptResponse>> getReadStatus(
            Authentication authentication,
            @PathVariable Long conversationId) {
        return ResponseEntity.ok(messageService.getReadStatus(authentication.getName(), conversationId));
    }
}