package com.vnguyenx.realtimechatai.service;

import com.vnguyenx.realtimechatai.dto.friend.FriendRequest;
import com.vnguyenx.realtimechatai.dto.friend.FriendResponse;
import com.vnguyenx.realtimechatai.entity.Friendship;
import com.vnguyenx.realtimechatai.entity.FriendshipStatus;
import com.vnguyenx.realtimechatai.entity.User;
import com.vnguyenx.realtimechatai.repository.FriendshipRepository;
import com.vnguyenx.realtimechatai.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class FriendshipService {

    private final FriendshipRepository friendshipRepository;
    private final UserRepository userRepository;
    private final UserValidationService userValidationService;
    private final ConversationService conversationService;

    public FriendshipService(FriendshipRepository friendshipRepository,
                              UserRepository userRepository,
                              UserValidationService userValidationService,
                              ConversationService conversationService) {
        this.friendshipRepository = friendshipRepository;
        this.userRepository = userRepository;
        this.userValidationService = userValidationService;
        this.conversationService = conversationService;
    }

    public FriendResponse sendRequest(String requesterUsername, FriendRequest request) {
        User requester = findUserOrThrow(requesterUsername);
        User addressee = findUserOrThrow(request.getTargetUsername());

        // MỚI — check TRƯỚC các rule khác, chặn sớm nhất có thể
        userValidationService.assertUserIsActive(requester);
        userValidationService.assertUserIsActive(addressee);

        // không tự kết bạn với chính mình
        if (requester.getId().equals(addressee.getId())) {
            throw new IllegalArgumentException("Không thể tự gửi lời mời kết bạn cho chính mình");
        }

        Optional<Friendship> existing = friendshipRepository.findFriendshipBetween(requester.getId(), addressee.getId());

        if (existing.isPresent()) {
            Friendship existingFriendship = existing.get();
            if (existingFriendship.getStatus() == FriendshipStatus.REJECTED) {
                // Cho gửi lại: cập nhật bản ghi cũ về PENDING, đổi lại đúng requester/addressee lần này
                existingFriendship.setRequester(requester);
                existingFriendship.setAddressee(addressee);
                existingFriendship.setStatus(FriendshipStatus.PENDING);
                existingFriendship.setUpdatedAt(LocalDateTime.now());
                Friendship saved = friendshipRepository.save(existingFriendship);
                return toResponse(saved, requester.getId());
            }
            // Còn PENDING hoặc đã ACCEPTED thì vẫn chặn như cũ
            throw new IllegalArgumentException("Đã tồn tại quan hệ kết bạn giữa 2 người này");
        }
    
        Friendship friendship = new Friendship();
        friendship.setRequester(requester);
        friendship.setAddressee(addressee);
        friendship.setStatus(FriendshipStatus.PENDING);

        Friendship saved = friendshipRepository.save(friendship);
        return toResponse(saved, requester.getId());
    }

    public FriendResponse acceptRequest(String currentUsername, Long friendshipId) {
        User currentUser = findUserOrThrow(currentUsername);
        Friendship friendship = findFriendshipOrThrow(friendshipId);

        if (!friendship.getAddressee().getId().equals(currentUser.getId())) {
            throw new IllegalArgumentException("Bạn không có quyền chấp nhận lời mời này");
        }
        if (friendship.getStatus() != FriendshipStatus.PENDING) {
            throw new IllegalArgumentException("Lời mời này đã được xử lý trước đó");
        }

        friendship.setStatus(FriendshipStatus.ACCEPTED);
        friendship.setUpdatedAt(LocalDateTime.now());

        Friendship saved = friendshipRepository.save(friendship);

        //tự động tạo Conversation ngay khi accept, giống Locket/Messenger
        conversationService.getOrCreateConversation(
                friendship.getRequester().getUsername(),
                friendship.getAddressee().getUsername()
        );

        return toResponse(saved, currentUser.getId());
    }

    public FriendResponse rejectRequest(String currentUsername, Long friendshipId) {
        User currentUser = findUserOrThrow(currentUsername);
        Friendship friendship = findFriendshipOrThrow(friendshipId);

        if (!friendship.getAddressee().getId().equals(currentUser.getId())) {
            throw new IllegalArgumentException("Bạn không có quyền từ chối lời mời này");
        }
        if (friendship.getStatus() != FriendshipStatus.PENDING) {
            throw new IllegalArgumentException("Lời mời này đã được xử lý trước đó");
        }

        friendship.setStatus(FriendshipStatus.REJECTED);
        friendship.setUpdatedAt(LocalDateTime.now());

        Friendship saved = friendshipRepository.save(friendship);
        return toResponse(saved, currentUser.getId());
    }

    public List<FriendResponse> getFriendList(String username) {
        User currentUser = findUserOrThrow(username);
        List<Friendship> friendships = friendshipRepository.findAllByUserIdAndStatus(
                currentUser.getId(), FriendshipStatus.ACCEPTED);

        return friendships.stream()
                .map(f -> toResponse(f, currentUser.getId()))
                .toList();
    }

    public List<FriendResponse> getPendingRequests(String username) {
        User currentUser = findUserOrThrow(username);
        List<Friendship> friendships = friendshipRepository.findByAddresseeIdAndStatus(
                currentUser.getId(), FriendshipStatus.PENDING);

        return friendships.stream()
                .map(f -> toResponse(f, currentUser.getId()))
                .toList();
    }

    private User findUserOrThrow(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("User không tồn tại: " + username));
    }

    private Friendship findFriendshipOrThrow(Long friendshipId) {
        return friendshipRepository.findById(friendshipId)
                .orElseThrow(() -> new IllegalArgumentException("Lời mời kết bạn không tồn tại"));
    }

    private FriendResponse toResponse(Friendship friendship, Long currentUserId) {
        User otherUser = friendship.getRequester().getId().equals(currentUserId)
                ? friendship.getAddressee()
                : friendship.getRequester();

        return new FriendResponse(
                friendship.getId(),
                otherUser.getId(),
                otherUser.getUsername(),
                otherUser.getFullName(),
                otherUser.getAvatarUrl(),
                friendship.getStatus(),
                friendship.getCreatedAt());
    }

    public List<FriendResponse> getSentRequests(String username) {
        User currentUser = findUserOrThrow(username);
        List<Friendship> friendships = friendshipRepository.findByRequesterIdAndStatus(
                currentUser.getId(), FriendshipStatus.PENDING);

        return friendships.stream()
                .map(f -> toResponse(f, currentUser.getId()))
                .toList();
    }

    public void cancelRequest(String currentUsername, Long friendshipId) {
        User currentUser = findUserOrThrow(currentUsername);
        Friendship friendship = findFriendshipOrThrow(friendshipId);

        // CHỈ người GỬI (requester) mới được huỷ
        if (!friendship.getRequester().getId().equals(currentUser.getId())) {
            throw new IllegalArgumentException("Bạn không có quyền huỷ lời mời này");
        }
        if (friendship.getStatus() != FriendshipStatus.PENDING) {
            throw new IllegalArgumentException("Lời mời này đã được xử lý, không thể huỷ");
        }

        friendshipRepository.delete(friendship); // XOÁ HẲN, không chuyển status
    }

}