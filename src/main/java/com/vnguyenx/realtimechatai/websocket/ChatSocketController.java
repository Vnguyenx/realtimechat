package com.vnguyenx.realtimechatai.websocket;

import com.vnguyenx.realtimechatai.dto.message.MessageResponse;
import com.vnguyenx.realtimechatai.dto.message.SendMessageRequest;
import com.vnguyenx.realtimechatai.service.MessageService;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import java.security.Principal;

@Controller
public class ChatSocketController {

    private final MessageService messageService;
    private final SimpMessagingTemplate messagingTemplate;

    public ChatSocketController(MessageService messageService, SimpMessagingTemplate messagingTemplate) {
        this.messageService = messageService;
        this.messagingTemplate = messagingTemplate;
    }

    @MessageMapping("/chat.send/{conversationId}")
        public void sendMessage(
        @DestinationVariable Long conversationId,
        SendMessageRequest request,
        Principal principal) {

        String username = principal.getName();

        MessageResponse userMessage = messageService.sendMessage(username, conversationId, request);
        messagingTemplate.convertAndSend("/topic/conversation." + conversationId, userMessage);
    }

}