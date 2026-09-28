package com.vnguyenx.realtimechatai.repository;

import com.vnguyenx.realtimechatai.entity.User;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByUsername(String username);
    Optional<User> findByEmail(String email);
    boolean existsByUsername(String username);
    boolean existsByEmail(String email);

    // route search — CHỈ trả về user KHÔNG bị ban
    List<User> findByUsernameContainingIgnoreCaseAndIsBannedFalse(String keyword);

    // route invite — CHỈ tìm thấy nếu user KHÔNG bị ban
    Optional<User> findByFriendInviteCodeAndIsBannedFalse(String code);

    boolean existsByFriendInviteCode(String code);
}