package com.vnguyenx.realtimechatai.repository;

import com.vnguyenx.realtimechatai.entity.Conversation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ConversationRepository extends JpaRepository<Conversation, Long> {

    // Tìm conversation đã tồn tại giữa 2 user cụ thể (đã chuẩn hoá thứ tự trước khi gọi)
    Optional<Conversation> findByUserAIdAndUserBId(Long userAId, Long userBId);

    // Danh sách TẤT CẢ conversation mà user này tham gia — vẫn cần OR vì user có thể là A hoặc B tuỳ dòng
    @Query("SELECT c FROM Conversation c WHERE c.userA.id = :userId OR c.userB.id = :userId " +
           "ORDER BY c.createdAt DESC")
    List<Conversation> findAllByUserId(@Param("userId") Long userId);
}