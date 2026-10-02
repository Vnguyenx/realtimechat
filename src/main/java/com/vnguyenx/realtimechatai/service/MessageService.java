package com.vnguyenx.realtimechatai.service;

import com.vnguyenx.realtimechatai.dto.message.MessageResponse;
import com.vnguyenx.realtimechatai.dto.message.SendMessageRequest;
import com.vnguyenx.realtimechatai.entity.Conversation;
import com.vnguyenx.realtimechatai.entity.ConversationMember;
import com.vnguyenx.realtimechatai.entity.Message;
import com.vnguyenx.realtimechatai.entity.User;
import com.vnguyenx.realtimechatai.repository.ConversationMemberRepository;
import com.vnguyenx.realtimechatai.repository.ConversationRepository;
import com.vnguyenx.realtimechatai.repository.MessageRepository;
import com.vnguyenx.realtimechatai.repository.StickerRepository;
import com.vnguyenx.realtimechatai.repository.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class MessageService {

    private final MessageRepository messageRepository;
    private final ConversationRepository conversationRepository;
    private final ConversationMemberRepository conversationMemberRepository;
    private final UserRepository userRepository;
    private final StickerRepository stickerRepository;
    private final KlipyService klipyService;

    public MessageService(MessageRepository messageRepository,
                           ConversationRepository conversationRepository,
                           ConversationMemberRepository conversationMemberRepository,
                           UserRepository userRepository,
                            StickerRepository stickerRepository,
                            KlipyService klipyService) {
        this.messageRepository = messageRepository;
        this.conversationRepository = conversationRepository;
        this.conversationMemberRepository = conversationMemberRepository;
        this.userRepository = userRepository;
        this.stickerRepository = stickerRepository;
        this.klipyService = klipyService;
    }

@Transactional
public MessageResponse sendMessage(String senderUsername, Long conversationId, SendMessageRequest request) {
    User sender = findUserOrThrow(senderUsername);
    Conversation conversation = findConversationOrThrow(conversationId);

    assertParticipant(conversation.getId(), sender.getId());
    String finalContent = resolveContent(sender, request);

    Message message = new Message();
    message.setConversation(conversation);
    message.setSender(sender);
    message.setContent(finalContent);
    message.setMessageType(request.getMessageType());
    message.setIsRead(false);

    Message saved = messageRepository.save(message);

    updateConversationAfterNewMessage(conversation, sender.getId(), buildLastMessagePreview(request));

    return toResponse(saved);
}

// vừa validate vừa TRẢ VỀ nội dung thật sự cần lưu
private String resolveContent(User sender, SendMessageRequest request) {
    String type = request.getMessageType();

    if ("IMAGE".equals(type)) {
        boolean isValidImageUrl = request.getContent()
                .matches("^https://nlbweyhylxzmvoirbzkd\\.supabase\\.co/storage/.*$");
        if (!isValidImageUrl) {
            throw new IllegalArgumentException("URL ảnh không hợp lệ");
        }
        return request.getContent();
    }

    if ("STICKER".equals(type)) {
        if ("KLIPY".equals(request.getStickerSource())) {
            // Không gọi lại Klipy — chỉ kiểm tra URL đúng domain CDN của họ
            boolean isValidKlipyUrl = request.getContent().matches("^https://static\\.klipy\\.com/.*$");
            if (!isValidKlipyUrl) {
                throw new IllegalArgumentException("URL sticker không hợp lệ");
            }
            return request.getContent();
        }
        // nhánh LOCAL giữ nguyên như cũ
        if (!stickerRepository.existsByImageUrl(request.getContent())) {
            throw new IllegalArgumentException("Sticker không hợp lệ");
        }
        return request.getContent();
    }
    return type;
}

private String buildLastMessagePreview(SendMessageRequest request) {
    return switch (request.getMessageType()) {
        case "IMAGE" -> "[Hình ảnh]";
        case "STICKER" -> "[Sticker]";
        default -> request.getContent();
    };
}

    public Page<MessageResponse> getMessages(String username, Long conversationId, Pageable pageable) {
        User currentUser = findUserOrThrow(username);
        assertParticipant(conversationId, currentUser.getId());

        return messageRepository.findByConversationIdOrderByCreatedAtDesc(conversationId, pageable)
                .map(this::toResponse);
    }

    public void markAsRead(String username, Long conversationId) {
        User currentUser = findUserOrThrow(username);

        ConversationMember member = conversationMemberRepository
                .findByConversationIdAndUserId(conversationId, currentUser.getId())
                .orElseThrow(() -> new IllegalArgumentException("Bạn không có quyền truy cập cuộc trò chuyện này"));

        member.setUnreadCount(0);
        conversationMemberRepository.save(member);
    }

    private void updateConversationAfterNewMessage(Conversation conversation, Long senderId, String content) {
        conversation.setLastMessage(content);
        conversation.setLastMessageAt(LocalDateTime.now());
        conversationRepository.save(conversation);

        conversationMemberRepository.incrementUnreadForOthers(conversation.getId(), senderId);
    }

    private void assertParticipant(Long conversationId, Long userId) {
        boolean isParticipant = conversationMemberRepository.existsByConversationIdAndUserId(conversationId, userId);
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