package com.vnguyenx.realtimechatai.dto.friend;

import com.vnguyenx.realtimechatai.entity.FriendshipStatus;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
public class FriendResponse {
    private Long friendshipId;
    private Long userId;       // id của NGƯỜI KIA (không phải chính mình)
    private String username;
    private String fullName;
    private String avatarUrl;
    private FriendshipStatus status;
    private LocalDateTime createdAt;
}