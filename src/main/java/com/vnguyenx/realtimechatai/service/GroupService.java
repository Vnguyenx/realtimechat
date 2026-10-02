package com.vnguyenx.realtimechatai.service;

import com.vnguyenx.realtimechatai.dto.conversation.*;
import com.vnguyenx.realtimechatai.entity.*;
import com.vnguyenx.realtimechatai.repository.ConversationMemberRepository;
import com.vnguyenx.realtimechatai.repository.ConversationRepository;
import com.vnguyenx.realtimechatai.repository.FriendshipRepository;
import com.vnguyenx.realtimechatai.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;

@Service
public class GroupService {

    private final ConversationRepository conversationRepository;
    private final ConversationMemberRepository conversationMemberRepository;
    private final UserRepository userRepository;
    private final FriendshipRepository friendshipRepository;
    private final UserValidationService userValidationService;

    public GroupService(ConversationRepository conversationRepository,
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
    public GroupResponse createGroup(String creatorUsername, CreateGroupRequest request) {
        User creator = findUserOrThrow(creatorUsername);
        userValidationService.assertUserIsActive(creator);

        Conversation conversation = new Conversation();
        conversation.setType(ConversationType.GROUP);
        conversation.setName(request.getName());
        Conversation saved = conversationRepository.save(conversation);

        addMemberInternal(saved, creator, MemberRole.OWNER);

        for (String username : request.getMemberUsernames()) {
            if (username.equals(creatorUsername)) continue; // bỏ qua nếu lỡ tự thêm chính mình

            User member = findUserOrThrow(username);
            userValidationService.assertUserIsActive(member);
            assertFriend(creator.getId(), member.getId(), "Chỉ có thể thêm bạn bè vào nhóm: " + username);

            addMemberInternal(saved, member, MemberRole.MEMBER);
        }

        return toGroupResponse(saved);
    }

    @Transactional
    public GroupResponse addMember(String actorUsername, Long conversationId, AddGroupMemberRequest request) {
        User actor = findUserOrThrow(actorUsername);
        Conversation conversation = findGroupOrThrow(conversationId);
        assertIsMember(conversationId, actor.getId());

        User target = findUserOrThrow(request.getTargetUsername());
        userValidationService.assertUserIsActive(target);

        if (conversationMemberRepository.existsByConversationIdAndUserId(conversationId, target.getId())) {
            throw new IllegalArgumentException("Người này đã ở trong nhóm");
        }

        // Rule: target phải là bạn bè của NGƯỜI THÊM (actor), không cần là bạn của mọi thành viên khác
        assertFriend(actor.getId(), target.getId(), "Chỉ có thể thêm bạn bè của bạn vào nhóm");

        addMemberInternal(conversation, target, MemberRole.MEMBER);
        return toGroupResponse(conversation);
    }

    public GroupResponse kickMember(String actorUsername, Long conversationId, String targetUsername) {
        User actor = findUserOrThrow(actorUsername);
        Conversation conversation = findGroupOrThrow(conversationId);
        assertIsOwner(conversationId, actor.getId());

        User target = findUserOrThrow(targetUsername);
        if (target.getId().equals(actor.getId())) {
            throw new IllegalArgumentException("Không thể tự kick chính mình — dùng chức năng rời nhóm");
        }

        ConversationMember targetMember = conversationMemberRepository
                .findByConversationIdAndUserId(conversationId, target.getId())
                .orElseThrow(() -> new IllegalArgumentException("Người này không ở trong nhóm"));

        conversationMemberRepository.delete(targetMember);
        return toGroupResponse(conversation);
    }

    @Transactional
    public void leaveGroup(String username, Long conversationId) {
        User user = findUserOrThrow(username);
        findGroupOrThrow(conversationId);

        ConversationMember myMembership = conversationMemberRepository
                .findByConversationIdAndUserId(conversationId, user.getId())
                .orElseThrow(() -> new IllegalArgumentException("Bạn không ở trong nhóm này"));

        boolean wasOwner = myMembership.getRole() == MemberRole.OWNER;
        conversationMemberRepository.delete(myMembership);

        // Nếu owner rời đi mà nhóm vẫn còn người, chuyển quyền owner cho người vào nhóm sớm nhất
        if (wasOwner) {
            List<ConversationMember> remaining = conversationMemberRepository.findByConversationId(conversationId);
            remaining.stream()
                    .min(Comparator.comparing(ConversationMember::getJoinedAt))
                    .ifPresent(next -> {
                        next.setRole(MemberRole.OWNER);
                        conversationMemberRepository.save(next);
                    });
            // Nếu remaining rỗng: nhóm còn lại "trống", không ai truy cập được — chấp nhận được, không cần xoá nhóm
        }
    }

    public GroupResponse renameGroup(String username, Long conversationId, RenameGroupRequest request) {
        User user = findUserOrThrow(username);
        Conversation conversation = findGroupOrThrow(conversationId);
        assertIsMember(conversationId, user.getId());

        conversation.setName(request.getName());
        conversationRepository.save(conversation);

        return toGroupResponse(conversation);
    }

    private void addMemberInternal(Conversation conversation, User user, MemberRole role) {
        ConversationMember member = new ConversationMember();
        member.setConversation(conversation);
        member.setUser(user);
        member.setRole(role);
        conversationMemberRepository.save(member);
    }

    private void assertFriend(Long userId1, Long userId2, String errorMessage) {
        Friendship friendship = friendshipRepository.findFriendshipBetween(userId1, userId2)
                .orElseThrow(() -> new IllegalArgumentException(errorMessage));
        if (friendship.getStatus() != FriendshipStatus.ACCEPTED) {
            throw new IllegalArgumentException(errorMessage);
        }
    }

    private void assertIsMember(Long conversationId, Long userId) {
        if (!conversationMemberRepository.existsByConversationIdAndUserId(conversationId, userId)) {
            throw new IllegalArgumentException("Bạn không thuộc nhóm này");
        }
    }

    private void assertIsOwner(Long conversationId, Long userId) {
        ConversationMember member = conversationMemberRepository.findByConversationIdAndUserId(conversationId, userId)
                .orElseThrow(() -> new IllegalArgumentException("Bạn không thuộc nhóm này"));
        if (member.getRole() != MemberRole.OWNER) {
            throw new IllegalArgumentException("Chỉ chủ nhóm mới có quyền này");
        }
    }

    private Conversation findGroupOrThrow(Long id) {
        Conversation conversation = conversationRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Nhóm không tồn tại"));
        if (conversation.getType() != ConversationType.GROUP) {
            throw new IllegalArgumentException("Cuộc trò chuyện này không phải nhóm");
        }
        return conversation;
    }

    private User findUserOrThrow(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("User không tồn tại: " + username));
    }

    private GroupResponse toGroupResponse(Conversation conversation) {
        List<ConversationMember> members = conversationMemberRepository.findByConversationId(conversation.getId());

        List<GroupMemberResponse> memberResponses = members.stream()
                .map(m -> new GroupMemberResponse(
                        m.getUser().getId(),
                        m.getUser().getUsername(),
                        m.getUser().getFullName(),
                        m.getUser().getAvatarUrl(),
                        m.getNickname(),
                        m.getRole().name()
                ))
                .toList();

        return new GroupResponse(
                conversation.getId(),
                conversation.getName(),
                memberResponses,
                conversation.getCreatedAt()
        );
    }
}