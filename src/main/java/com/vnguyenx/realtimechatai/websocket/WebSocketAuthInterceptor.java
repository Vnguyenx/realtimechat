package com.vnguyenx.realtimechatai.websocket;

import com.vnguyenx.realtimechatai.security.JwtService;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class WebSocketAuthInterceptor implements ChannelInterceptor {

    private final JwtService jwtService;

    public WebSocketAuthInterceptor(JwtService jwtService) {
        this.jwtService = jwtService;
    }

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        // ĐIỂM MẤU CHỐT: dùng getAccessor(), KHÔNG dùng wrap()
        StompHeaderAccessor accessor =
                MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);

        if (accessor != null && StompCommand.CONNECT.equals(accessor.getCommand())) {
            List<String> authHeaders = accessor.getNativeHeader("Authorization");

            if (authHeaders != null && !authHeaders.isEmpty()) {
                String token = authHeaders.get(0).substring(7);

                if (jwtService.isTokenValid(token)) {
                    String username = jwtService.extractUsername(token);
                    accessor.setUser(new UsernamePasswordAuthenticationToken(username, null, List.of()));

                    // BẮT BUỘC: cho phép header vẫn "mutable" sau khi ra khỏi interceptor này,
                    // để thay đổi thực sự được giữ lại trong pipeline xử lý tiếp theo
                    accessor.setLeaveMutable(true);
                }
            }
        }

        return message; // trả về message GỐC — vì giờ nó dùng chung accessor đã sửa, không cần rebuild
    }
}