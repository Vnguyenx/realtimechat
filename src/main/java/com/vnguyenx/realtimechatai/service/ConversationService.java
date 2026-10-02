package com.vnguyenx.realtimechatai.service;

import com.vnguyenx.realtimechatai.dto.conversation.ConversationResponse;
import com.vnguyenx.realtimechatai.dto.conversation.ConversationSummaryResponse;
import com.vnguyenx.realtimechatai.entity.Conversation;
import com.vnguyenx.realtimechatai.entity.ConversationMember;
import com.vnguyenx.realtimechatai.entity.ConversationType;
import com.vnguyenx.realtimechatai.entity.Friendship;
import com.vnguyenx.realtimechatai.entity.FriendshipStatus;
import com.vnguyenx.realtimechatai.entity.User;
import com.vnguyenx.realtimechatai.repository.ConversationMemberRepository;
import com.vnguyenx.realtimechatai.repository.ConversationRepository;
import com.vnguyenx.realtimechatai.repository.FriendshipRepository;
import com.vnguyenx.realtimechatai.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.vnguyenx.realtimechatai.dto.conversation.UpdateNicknameRequest;

import java.util.List;

@Service
public class ConversationService {

    private final ConversationRepository conversationRepository;
    private final ConversationMemberRepository conversationMemberRepository;
    private final UserRepository userRepository;
    private final FriendshipRepository friendshipRepository;
    private final UserValidationService userValidationService;


    public ConversationService(ConversationRepository conversationRepository,
                                ConversationMemberRepository conversationMemberRepository,
                                UserRepository userRepository,
                                FriendshipRepository friendshipRepository,
                                UserValidationService userValidationService) {
        this.conversationRepository = conversationRepository;
        this.conversationMemberRepository = conversationMemberRepository;
        this.userRepository = userRepository;
        this.friendshipRepository = friendshipRepository;
        this.userValidationService = userValidationService;
    }

    @Transactional
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

        String directKey = buildDirectKey(currentUser.getId(), targetUser.getId());

        Conversation conversation = conversationRepository.findByDirectKey(directKey)
                .orElseGet(() -> createDirectConversation(directKey, currentUser, targetUser));

        return toResponse(conversation, currentUser.getId());
    }

    public List<ConversationSummaryResponse> getMyConversations(String username) {
        User currentUser = findUserOrThrow(username);
        List<Conversation> conversations = conversationRepository.findAllByMemberUserId(currentUser.getId());

        return conversations.stream()
                .map(c -> toSummaryResponse(c, currentUser.getId()))
                .toList();
    }

    private ConversationSummaryResponse toSummaryResponse(Conversation conversation, Long currentUserId) {
        List<ConversationMember> members = conversationMemberRepository.findByConversationId(conversation.getId());

        ConversationMember myMembership = members.stream()
                .filter(m -> m.getUser().getId().equals(currentUserId))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("Bạn không thuộc cuộc trò chuyện này"));

        String displayName;
        String avatarUrl;
        Integer memberCount;

        if (conversation.getType() == ConversationType.GROUP) {
            displayName = conversation.getName();
            avatarUrl = null;
            memberCount = members.size();
        } else {
            ConversationMember friendMember = members.stream()
                    .filter(m -> !m.getUser().getId().equals(currentUserId))
                    .findFirst()
                    .orElseThrow(() -> new IllegalStateException("Cuộc trò chuyện thiếu thành viên"));

            User friend = friendMember.getUser();
            displayName = friendMember.getNickname() != null
                    ? friendMember.getNickname()
                    : (friend.getFullName() != null ? friend.getFullName() : friend.getUsername());
            avatarUrl = friend.getAvatarUrl();
            memberCount = null;
        }

        return new ConversationSummaryResponse(
                conversation.getId(),
                conversation.getType().name(),
                displayName,
                avatarUrl,
                memberCount,
                conversation.getLastMessage(),
                conversation.getLastMessageAt(),
                myMembership.getUnreadCount(),
                conversation.getCreatedAt());
    }


    private Conversation createDirectConversation(String directKey, User userA, User userB) {
        Conversation conversation = new Conversation();
        conversation.setType(ConversationType.DIRECT);
        conversation.setDirectKey(directKey);
        Conversation saved = conversationRepository.save(conversation);

        addMember(saved, userA);
        addMember(saved, userB);

        return saved;
    }

    private void addMember(Conversation conversation, User user) {
        ConversationMember member = new ConversationMember();
        member.setConversation(conversation);
        member.setUser(user);
        conversationMemberRepository.save(member);
    }

    private String buildDirectKey(Long id1, Long id2) {
        long min = Math.min(id1, id2);
        long max = Math.max(id1, id2);
        return min + "_" + max;
    }

    private User findUserOrThrow(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("User không tồn tại: " + username));
    }

    public ConversationResponse updateNickname(String currentUsername, Long conversationId, UpdateNicknameRequest request) {
        User currentUser = findUserOrThrow(currentUsername);
        userValidationService.assertUserIsActive(currentUser);

    Conversation conversation = conversationRepository.findById(conversationId)
            .orElseThrow(() -> new IllegalArgumentException("Cuộc trò chuyện không tồn tại"));

    // Xác nhận currentUser thuộc conversation này
    conversationMemberRepository.findByConversationIdAndUserId(conversationId, currentUser.getId())
            .orElseThrow(() -> new IllegalArgumentException("Bạn không có quyền truy cập cuộc trò chuyện này"));

    User target = findUserOrThrow(request.getTargetUsername());

    // Tìm đúng dòng thành viên của TARGET — nếu không có, nghĩa là target không thuộc conversation
    ConversationMember targetMember = conversationMemberRepository
            .findByConversationIdAndUserId(conversationId, target.getId())
            .orElseThrow(() -> new IllegalArgumentException("Người này không thuộc cuộc trò chuyện"));

    String nickname = (request.getNickname() == null || request.getNickname().isBlank())
            ? null
            : request.getNickname().trim();

    targetMember.setNickname(nickname);
    conversationMemberRepository.save(targetMember);

    return toResponse(conversation, currentUser.getId());
}

// Sửa lại toResponse() — thêm lấy nickname
private ConversationResponse toResponse(Conversation conversation, Long currentUserId) {
    List<ConversationMember> members = conversationMemberRepository.findByConversationId(conversation.getId());

    ConversationMember friendMember = members.stream()
            .filter(m -> !m.getUser().getId().equals(currentUserId))
            .findFirst()
            .orElseThrow(() -> new IllegalStateException("Cuộc trò chuyện thiếu thành viên"));

    ConversationMember myMember = members.stream()
            .filter(m -> m.getUser().getId().equals(currentUserId))
            .findFirst()
            .orElseThrow(() -> new IllegalStateException("Bạn không thuộc cuộc trò chuyện này"));

    User friend = friendMember.getUser();

    return new ConversationResponse(
            conversation.getId(),
            friend.getId(),
            friend.getUsername(),
            friend.getFullName(),
            friend.getAvatarUrl(),
            friendMember.getNickname(),
            myMember.getNickname(),
            conversation.getCreatedAt()
    );
}
}