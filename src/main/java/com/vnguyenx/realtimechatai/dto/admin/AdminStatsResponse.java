package com.vnguyenx.realtimechatai.dto.admin;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class AdminStatsResponse {
    private long totalUsers;
    private long totalBannedUsers;
    private long totalMessages;
    private long totalDirectConversations;
    private long totalGroups;
    private long totalAcceptedFriendships;
}