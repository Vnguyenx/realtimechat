package com.vnguyenx.realtimechatai.security;

import com.vnguyenx.realtimechatai.entity.User;
import com.vnguyenx.realtimechatai.repository.UserRepository;

import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;


@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    public CustomUserDetailsService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("Không tìm thấy user: " + username));

        // Spring Security cần 1 object kiểu UserDetails, không dùng trực tiếp Entity User của bạn
        return org.springframework.security.core.userdetails.User.builder()
                .username(user.getUsername())
                .password(user.getPassword()) // password đã hash sẵn trong DB
                .authorities(new SimpleGrantedAuthority("ROLE_" + user.getRole())) // ROLE_CUSTOMER hoặc ROLE_ADMIN
                .build();
    }
}