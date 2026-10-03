package com.vnguyenx.realtimechatai.service;

import com.vnguyenx.realtimechatai.dto.admin.*;
import com.vnguyenx.realtimechatai.entity.ConversationType;
import com.vnguyenx.realtimechatai.entity.FriendshipStatus;
import com.vnguyenx.realtimechatai.entity.User;
import com.vnguyenx.realtimechatai.repository.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
public class AdminService {

    private final UserRepository userRepository;
    private final ConversationRepository conversationRepository;
    private final MessageRepository messageRepository;
    private final FriendshipRepository friendshipRepository;

    public AdminService(UserRepository userRepository,
                         ConversationRepository conversationRepository,
                         MessageRepository messageRepository,
                         FriendshipRepository friendshipRepository) {
        this.userRepository = userRepository;
        this.conversationRepository = conversationRepository;
        this.messageRepository = messageRepository;
        this.friendshipRepository = friendshipRepository;
    }

    public Page<AdminUserSummaryResponse> getUsers(String keyword, Pageable pageable) {
        String kw = (keyword == null) ? "" : keyword;
        return userRepository
                .findByUsernameContainingIgnoreCaseOrEmailContainingIgnoreCase(kw, kw, pageable)
                .map(this::toSummary);
    }

    public AdminUserDetailResponse getUserDetail(Long userId) {
        User user = findUserOrThrow(userId);
        return toDetail(user);
    }

    public AdminUserDetailResponse banUser(Long userId) {
        User user = findUserOrThrow(userId);
        user.setIsBanned(true);
        userRepository.save(user);
        return toDetail(user);
    }

    public AdminUserDetailResponse unbanUser(Long userId) {
        User user = findUserOrThrow(userId);
        user.setIsBanned(false);
        userRepository.save(user);
        return toDetail(user);
    }

    public AdminStatsResponse getStats() {
        return new AdminStatsResponse(
                userRepository.count(),
                userRepository.countByIsBannedTrue(),
                messageRepository.count(),
                conversationRepository.countByType(ConversationType.DIRECT),
                conversationRepository.countByType(ConversationType.GROUP),
                friendshipRepository.countByStatus(FriendshipStatus.ACCEPTED)
        );
    }

    private User findUserOrThrow(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("User không tồn tại"));
    }

    private AdminUserSummaryResponse toSummary(User u) {
        return new AdminUserSummaryResponse(
                u.getId(), u.getUsername(), u.getEmail(), u.getFullName(),
                u.getRole(), u.getIsBanned(), u.getIsActive(), u.getCreatedAt());
    }

    private AdminUserDetailResponse toDetail(User u) {
        return new AdminUserDetailResponse(
                u.getId(), u.getUsername(), u.getEmail(), u.getFullName(), u.getAvatarUrl(),
                u.getBirthday(), u.getRole(), u.getIsBanned(), u.getIsActive(),
                u.getCreatedAt(), u.getUpdatedAt());
    }
}