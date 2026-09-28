package com.vnguyenx.realtimechatai.service;

import com.vnguyenx.realtimechatai.dto.message.MessageResponse;
import com.vnguyenx.realtimechatai.dto.message.SendMessageRequest;
import com.vnguyenx.realtimechatai.entity.Conversation;
import com.vnguyenx.realtimechatai.entity.Message;
import com.vnguyenx.realtimechatai.entity.User;
import com.vnguyenx.realtimechatai.repository.ConversationRepository;
import com.vnguyenx.realtimechatai.repository.MessageRepository;
import com.vnguyenx.realtimechatai.repository.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class MessageService {

    private final MessageRepository messageRepository;
    private final ConversationRepository conversationRepository;
    private final UserRepository userRepository;

    public MessageService(MessageRepository messageRepository,
                           ConversationRepository conversationRepository,
                           UserRepository userRepository) {
        this.messageRepository = messageRepository;
        this.conversationRepository = conversationRepository;
        this.userRepository = userRepository;
    }

    public MessageResponse sendMessage(String senderUsername, Long conversationId, SendMessageRequest request) {
        User sender = findUserOrThrow(senderUsername);
        Conversation conversation = findConversationOrThrow(conversationId);

        assertParticipant(conversation, sender.getId());
        validateContentForType(request.getMessageType(), request.getContent()); // MỚI

        Message message = new Message();
        message.setConversation(conversation);
        message.setSender(sender);
        message.setContent(request.getContent());
        message.setMessageType(request.getMessageType());
        message.setIsRead(false);

        Message saved = messageRepository.save(message);

        updateConversationAfterNewMessage(conversation, sender.getId(), buildLastMessagePreview(request));

        return toResponse(saved);
    }
    
    private void validateContentForType(String messageType, String content) {
        if ("IMAGE".equals(messageType)) {
            boolean isValidImageUrl = content.matches("^https://nlbweyhylxzmvoirbzkd\\.supabase\\.co/storage/.*$");
            if (!isValidImageUrl) {
                throw new IllegalArgumentException("URL ảnh không hợp lệ");
            }
        }
    }

    private String buildLastMessagePreview(SendMessageRequest request) {
        return "IMAGE".equals(request.getMessageType()) ? "[Hình ảnh]" : request.getContent();
    }


    public Page<MessageResponse> getMessages(String username, Long conversationId, Pageable pageable) {
        User currentUser = findUserOrThrow(username);
        Conversation conversation = findConversationOrThrow(conversationId);

        assertParticipant(conversation, currentUser.getId());

        return messageRepository.findByConversationIdOrderByCreatedAtDesc(conversationId, pageable)
                .map(this::toResponse);
    }

    public void markAsRead(String username, Long conversationId) {
        User currentUser = findUserOrThrow(username);
        Conversation conversation = findConversationOrThrow(conversationId);

        assertParticipant(conversation, currentUser.getId());

        if (conversation.getUserA().getId().equals(currentUser.getId())) {
            conversation.setUnreadCountA(0);
        } else {
            conversation.setUnreadCountB(0);
        }
        conversationRepository.save(conversation);
    }

    private void updateConversationAfterNewMessage(Conversation conversation, Long senderId, String content) {
        conversation.setLastMessage(content);
        conversation.setLastMessageAt(LocalDateTime.now());

        if (conversation.getUserA().getId().equals(senderId)) {
            conversation.setUnreadCountB(conversation.getUnreadCountB() + 1);
        } else {
            conversation.setUnreadCountA(conversation.getUnreadCountA() + 1);
        }

        conversationRepository.save(conversation);
    }

    private void assertParticipant(Conversation conversation, Long userId) {
        boolean isParticipant = conversation.getUserA().getId().equals(userId)
                || conversation.getUserB().getId().equals(userId);
        if (!isParticipant) {
            throw new IllegalArgumentException("Bạn không có quyền truy cập cuộc trò chuyện này");
        }
    }

    private User findUserOrThrow(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("User không tồn tại: " + username));
    }

    private Conversation findConversationOrThrow(Long id) {
        return conversationRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Cuộc trò chuyện không tồn tại"));
    }

    private MessageResponse toResponse(Message message) {
        return new MessageResponse(
                message.getId(),
                message.getSender().getId(),
                message.getSender().getUsername(),
                message.getContent(),
                message.getMessageType(),
                message.getIsRead(),
                message.getCreatedAt());
    }
}