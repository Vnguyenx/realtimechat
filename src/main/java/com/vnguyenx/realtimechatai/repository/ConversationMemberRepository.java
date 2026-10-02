package com.vnguyenx.realtimechatai.repository;

import com.vnguyenx.realtimechatai.entity.ConversationMember;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ConversationMemberRepository extends JpaRepository<ConversationMember, Long> {

    List<ConversationMember> findByConversationId(Long conversationId);

    Optional<ConversationMember> findByConversationIdAndUserId(Long conversationId, Long userId);

    boolean existsByConversationIdAndUserId(Long conversationId, Long userId);

    //tăng unreadCount cho MỌI thành viên TRỪ người gửi, chạy 1 câu UPDATE duy nhất
    @Modifying
    @Query("UPDATE ConversationMember m SET m.unreadCount = m.unreadCount + 1 " +
           "WHERE m.conversation.id = :conversationId AND m.user.id <> :senderId")
    void incrementUnreadForOthers(@Param("conversationId") Long conversationId,
                                   @Param("senderId") Long senderId);
}