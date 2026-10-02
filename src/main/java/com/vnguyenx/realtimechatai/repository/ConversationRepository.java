package com.vnguyenx.realtimechatai.repository;

import com.vnguyenx.realtimechatai.entity.Conversation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ConversationRepository extends JpaRepository<Conversation, Long> {

    Optional<Conversation> findByDirectKey(String directKey);

    @Query("SELECT c FROM Conversation c JOIN ConversationMember m ON m.conversation = c " +
           "WHERE m.user.id = :userId ORDER BY c.createdAt DESC")
    List<Conversation> findAllByMemberUserId(@Param("userId") Long userId);
}