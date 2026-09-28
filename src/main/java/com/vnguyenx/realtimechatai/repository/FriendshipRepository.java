package com.vnguyenx.realtimechatai.repository;

import com.vnguyenx.realtimechatai.entity.Friendship;
import com.vnguyenx.realtimechatai.entity.FriendshipStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface FriendshipRepository extends JpaRepository<Friendship, Long> {

    // Tìm 1 quan hệ cụ thể giữa 2 user theo đúng chiều (A gửi cho B)
    Optional<Friendship> findByRequesterIdAndAddresseeId(Long requesterId, Long addresseeId);

    // Kiểm tra đã tồn tại quan hệ (bất kể chiều nào) giữa 2 user chưa —
    // dùng JPQL vì Query Method không tự diễn đạt được điều kiện "OR" theo 2 chiều
    @Query("SELECT f FROM Friendship f WHERE " +
           "(f.requester.id = :userA AND f.addressee.id = :userB) OR " +
           "(f.requester.id = :userB AND f.addressee.id = :userA)")
    Optional<Friendship> findFriendshipBetween(@Param("userA") Long userA, @Param("userB") Long userB);

    // Danh sách bạn bè thật sự (status = ACCEPTED), lấy cả 2 chiều
    @Query("SELECT f FROM Friendship f WHERE " +
           "(f.requester.id = :userId OR f.addressee.id = :userId) AND f.status = :status")
    List<Friendship> findAllByUserIdAndStatus(@Param("userId") Long userId, @Param("status") FriendshipStatus status);

    // Danh sách lời mời ĐANG CHỜ mà user này là NGƯỜI NHẬN (để hiển thị "Lời mời kết bạn")
    List<Friendship> findByAddresseeIdAndStatus(Long addresseeId, FriendshipStatus status);

    // Danh sách lời mời ĐANG CHỜ mà user này là NGƯỜI GỬI (để hiển thị "Lời mời đã gửi")
    List<Friendship> findByRequesterIdAndStatus(Long requesterId, FriendshipStatus status);
}