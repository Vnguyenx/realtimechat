package com.vnguyenx.realtimechatai.service;

import com.vnguyenx.realtimechatai.dto.conversation.ConversationResponse;
import com.vnguyenx.realtimechatai.entity.Conversation;
import com.vnguyenx.realtimechatai.entity.Friendship;
import com.vnguyenx.realtimechatai.entity.FriendshipStatus;
import com.vnguyenx.realtimechatai.entity.User;
import com.vnguyenx.realtimechatai.repository.ConversationRepository;
import com.vnguyenx.realtimechatai.repository.FriendshipRepository;
import com.vnguyenx.realtimechatai.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ConversationService {

    private final ConversationRepository conversationRepository;
    private final UserRepository userRepository;
    private final FriendshipRepository friendshipRepository;
    private final UserValidationService userValidationService;

    public ConversationService(ConversationRepository conversationRepository,
                                UserRepository userRepository,
                                FriendshipRepository friendshipRepository,
                                UserValidationService userValidationService) {
        this.conversationRepository = conversationRepository;
        this.userRepository = userRepository;
        this.friendshipRepository = friendshipRepository;
        this.userValidationService = userValidationService;
    }

    public ConversationResponse getOrCreateConversation(String currentUsername, String targetUsername) {
        User currentUser = findUserOrThrow(currentUsername);
        User targetUser = findUserOrThrow(targetUsername);

        userValidationService.assertUserIsActive(currentUser);
        userValidationService.assertUserIsActive(targetUser);

        if (currentUser.getId().equals(targetUser.getId())) {
            throw new IllegalArgumentException("Không thể tạo cuộc trò chuyện với chính mình");
        }

        Friendship friendship = friendshipRepository
                .findFriendshipBetween(currentUser.getId(), targetUser.getId())
                .orElseThrow(() -> new IllegalArgumentException("Chỉ có thể nhắn tin với bạn bè"));

        if (friendship.getStatus() != FriendshipStatus.ACCEPTED) {
            throw new IllegalArgumentException("Chỉ có thể nhắn tin với bạn bè");
        }

        User userA = currentUser.getId() < targetUser.getId() ? currentUser : targetUser;
        User userB = currentUser.getId() < targetUser.getId() ? targetUser : currentUser;

        Conversation conversation = conversationRepository
                .findByUserAIdAndUserBId(userA.getId(), userB.getId())
                .orElseGet(() -> {
                    Conversation newConv = new Conversation();
                    newConv.setUserA(userA);
                    newConv.setUserB(userB);
                    return conversationRepository.save(newConv);
                });

        return toResponse(conversation, currentUser.getId());
    }

    public List<ConversationResponse> getMyConversations(String username) {
        User currentUser = findUserOrThrow(username);
        List<Conversation> conversations = conversationRepository.findAllByUserId(currentUser.getId());

        return conversations.stream()
                .map(c -> toResponse(c, currentUser.getId()))
                .toList();
    }

    private User findUserOrThrow(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("User không tồn tại: " + username));
    }

    private ConversationResponse toResponse(Conversation conversation, Long currentUserId) {
        User friend = conversation.getUserA().getId().equals(currentUserId)
                ? conversation.getUserB()
                : conversation.getUserA();

        return new ConversationResponse(
                conversation.getId(),
                friend.getId(),
                friend.getUsername(),
                friend.getFullName(),
                friend.getAvatarUrl(),
                conversation.getCreatedAt(),
                conversation.getLastMessage(),
                conversation.getLastMessageAt()
        );
    }
}