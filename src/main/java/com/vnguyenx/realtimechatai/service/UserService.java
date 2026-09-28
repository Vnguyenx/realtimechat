package com.vnguyenx.realtimechatai.service;

import com.vnguyenx.realtimechatai.dto.user.InvitePreviewResponse;
import com.vnguyenx.realtimechatai.dto.user.UpdateAvatarRequest;
import com.vnguyenx.realtimechatai.dto.user.UpdateProfileRequest;
import com.vnguyenx.realtimechatai.dto.user.UserProfileResponse;
import com.vnguyenx.realtimechatai.dto.user.UserSearchResponse;
import com.vnguyenx.realtimechatai.entity.User;
import com.vnguyenx.realtimechatai.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Period;
import java.util.List;

@Service
public class UserService {

    private static final int MIN_AGE = 13;
    private static final int MAX_AGE = 100;

    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public UserProfileResponse getCurrentUserProfile(String username) {
        User user = findUserOrThrow(username);
        return toResponse(user);
    }

    public UserProfileResponse updateProfile(String username, UpdateProfileRequest request) {
        User user = findUserOrThrow(username);

        validateAge(request.getBirthday());

        user.setFullName(request.getFullName().trim());
        user.setBirthday(request.getBirthday());
        user.setUpdatedAt(LocalDateTime.now());

        return toResponse(userRepository.save(user));
    }

    public UserProfileResponse updateAvatar(String username, UpdateAvatarRequest request) {
        User user = findUserOrThrow(username);

        user.setAvatarUrl(request.getAvatarUrl());
        user.setUpdatedAt(LocalDateTime.now());

        return toResponse(userRepository.save(user));
    }

    // Dùng chung cho cả 3 method trên — tránh lặp lại đoạn findByUsername().orElseThrow()
    private User findUserOrThrow(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("User không tồn tại"));
    }

    private void validateAge(LocalDate birthday) {
        int age = Period.between(birthday, LocalDate.now()).getYears();
        if (age < MIN_AGE || age > MAX_AGE) {
            throw new IllegalArgumentException(
                    "Ngày sinh không hợp lệ (tuổi phải từ " + MIN_AGE + " đến " + MAX_AGE + ")");
        }
    }

    private UserProfileResponse toResponse(User user) {
        return new UserProfileResponse(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.getFullName(),
                user.getAvatarUrl(),
                user.getBirthday(),
                user.getFriendInviteCode(),
                user.getCreatedAt());
    }

    public List<UserSearchResponse> searchUsers(String keyword) {
        List<User> users = userRepository.findByUsernameContainingIgnoreCaseAndIsBannedFalse(keyword); // ĐỔI
        return users.stream()
                .map(u -> new UserSearchResponse(u.getId(), u.getUsername(), u.getFullName(), u.getAvatarUrl()))
                .toList();
    }
    
    public InvitePreviewResponse getInvitePreview(String code) {
        User user = userRepository.findByFriendInviteCodeAndIsBannedFalse(code) // ĐỔI
                .orElseThrow(() -> new IllegalArgumentException("Mã mời không hợp lệ"));

        return new InvitePreviewResponse(user.getUsername(), user.getFullName(), user.getAvatarUrl());
    }

}