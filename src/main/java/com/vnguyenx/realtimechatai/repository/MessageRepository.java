package com.vnguyenx.realtimechatai.repository;

import com.vnguyenx.realtimechatai.entity.Message;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface MessageRepository extends JpaRepository<Message, Long> {

    // Pagination lazy-load — đúng yêu cầu Tier 1 "hiển thị 6 tin nhắn mỗi lần"
    Page<Message> findByConversationIdOrderByCreatedAtDesc(Long conversationId, Pageable pageable);

    Optional<Message> findFirstByConversationIdOrderByIdDesc(Long conversationId);
}