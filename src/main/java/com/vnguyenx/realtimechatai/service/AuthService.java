package com.vnguyenx.realtimechatai.service;

import com.vnguyenx.realtimechatai.dto.auth.AuthResponse;
import com.vnguyenx.realtimechatai.dto.auth.ForgotPasswordRequest;
import com.vnguyenx.realtimechatai.dto.auth.LoginRequest;
import com.vnguyenx.realtimechatai.dto.auth.RegisterRequest;
import com.vnguyenx.realtimechatai.dto.auth.ResetPasswordRequest;
import com.vnguyenx.realtimechatai.entity.PasswordResetToken;
import com.vnguyenx.realtimechatai.entity.User;
import com.vnguyenx.realtimechatai.repository.PasswordResetTokenRepository;
import com.vnguyenx.realtimechatai.repository.UserRepository;
import com.vnguyenx.realtimechatai.security.JwtService;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class AuthService {

     @Value("${app.reset-password.token-expiration-minutes}")
     private long tokenExpirationMinutes;
    
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final UserValidationService userValidationService;
    private final PasswordResetTokenRepository passwordResetTokenRepository;
    private final EmailService emailService;

    public AuthService(UserRepository userRepository,
                        PasswordEncoder passwordEncoder,
                        AuthenticationManager authenticationManager,
                        JwtService jwtService,
                        UserValidationService userValidationService,
                        PasswordResetTokenRepository passwordResetTokenRepository,
            EmailService emailService) {

        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
        this.userValidationService = userValidationService;
        this.passwordResetTokenRepository = passwordResetTokenRepository;
        this.emailService = emailService;
    }

    public AuthResponse register(RegisterRequest request) {
        if (!request.getPassword().equals(request.getRePassword())) {
            throw new IllegalArgumentException("Password và nhập lại password không khớp");
        }
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new IllegalArgumentException("Username đã tồn tại");
        }
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("Email đã được sử dụng");
        }

        User user = new User();
        user.setUsername(request.getUsername());
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setFriendInviteCode(generateUniqueInviteCode());
        user.setCreatedAt(LocalDateTime.now());
        user.setUpdatedAt(LocalDateTime.now());

        User savedUser = userRepository.save(user);

        String token = jwtService.generateToken(savedUser.getUsername(), savedUser.getRole());
        return new AuthResponse(token, savedUser.getId(), savedUser.getUsername(), savedUser.getRole());
    }

    private String generateUniqueInviteCode() {
        String code;
        do {
            code = java.util.UUID.randomUUID().toString().substring(0, 8);
        } while (userRepository.existsByFriendInviteCode(code));
        return code;
    }

    public AuthResponse login(LoginRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword()));

        User user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new IllegalArgumentException("User không tồn tại"));

        // MỚI — check NGAY SAU KHI xác thực password đúng, TRƯỚC KHI phát token
        userValidationService.assertUserIsActive(user);

        String token = jwtService.generateToken(user.getUsername(), user.getRole());
        return new AuthResponse(token, user.getId(), user.getUsername(), user.getRole());
    }
    
    public void forgotPassword(ForgotPasswordRequest request) {
        userRepository.findByEmail(request.getEmail()).ifPresent(user -> {
            String token = java.util.UUID.randomUUID().toString();

            PasswordResetToken resetToken = new PasswordResetToken();
            resetToken.setUser(user);
            resetToken.setToken(token);
            resetToken.setExpiresAt(LocalDateTime.now().plusMinutes(tokenExpirationMinutes));
            resetToken.setUsed(false);

            passwordResetTokenRepository.save(resetToken);

            try {
                emailService.sendPasswordResetEmail(user.getEmail(), token);
            } catch (Exception e) {
                // Gửi email thất bại (domain rác, mất mạng...) — KHÔNG được lộ ra ngoài,
                // token vẫn đã lưu trong DB, chỉ là email không tới nơi
                System.err.println("Không gửi được email reset password: " + e.getMessage());
            }
        });
    }

    public void resetPassword(ResetPasswordRequest request) {
        if (!request.getNewPassword().equals(request.getConfirmNewPassword())) {
            throw new IllegalArgumentException("Password và nhập lại password không khớp");
        }

        PasswordResetToken resetToken = passwordResetTokenRepository.findByToken(request.getToken())
                .orElseThrow(() -> new IllegalArgumentException("Token không hợp lệ"));

        if (resetToken.getUsed()) {
            throw new IllegalArgumentException("Token này đã được sử dụng");
        }
        if (resetToken.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException("Token đã hết hạn");
        }

        User user = resetToken.getUser();
        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        user.setUpdatedAt(LocalDateTime.now());
        userRepository.save(user);

        resetToken.setUsed(true);
        passwordResetTokenRepository.save(resetToken);
    }

}