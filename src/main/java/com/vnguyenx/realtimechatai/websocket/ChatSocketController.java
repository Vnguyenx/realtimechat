package com.vnguyenx.realtimechatai.websocket;

import com.vnguyenx.realtimechatai.dto.message.MessageResponse;
import com.vnguyenx.realtimechatai.dto.message.SendMessageRequest;
import com.vnguyenx.realtimechatai.service.MessageService;

import jakarta.validation.Valid;

import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageExceptionHandler;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.annotation.SendToUser;
import org.springframework.stereotype.Controller;
import org.springframework.messaging.handler.annotation.support.MethodArgumentNotValidException;
import java.util.Map;

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
        @Payload @Valid SendMessageRequest request,
                Principal principal) {

            String username = principal.getName();

            MessageResponse userMessage = messageService.sendMessage(username, conversationId, request);
            messagingTemplate.convertAndSend("/topic/conversation." + conversationId, userMessage);
        }

    @MessageExceptionHandler(IllegalArgumentException.class)
@SendToUser("/queue/errors")
public Map<String, String> handleBusinessError(IllegalArgumentException ex) {
    return Map.of("message", ex.getMessage());
}

@MessageExceptionHandler(MethodArgumentNotValidException.class)
@SendToUser("/queue/errors")
public Map<String, String> handleValidationError(MethodArgumentNotValidException ex) {
    return Map.of("message", "Dữ liệu tin nhắn không hợp lệ");
}


}