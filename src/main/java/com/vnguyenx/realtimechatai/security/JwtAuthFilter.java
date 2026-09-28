package com.vnguyenx.realtimechatai.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class JwtAuthFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final CustomUserDetailsService userDetailsService;

    public JwtAuthFilter(JwtService jwtService, CustomUserDetailsService userDetailsService) {
        this.jwtService = jwtService;
        this.userDetailsService = userDetailsService;
    }

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request,
                                     @NonNull HttpServletResponse response,
                                     @NonNull FilterChain filterChain) throws ServletException, IOException {

        String authHeader = request.getHeader("Authorization");

        // 1. Không có header, hoặc không đúng định dạng "Bearer xxx" → bỏ qua, cho request đi tiếp
        //    (route public như /api/auth/login sẽ không có header này, vẫn cần chạy được)
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        // 2. Cắt bỏ chữ "Bearer " (7 ký tự) để lấy token thô
        String token = authHeader.substring(7);

        // 3. Verify token hợp lệ không (đúng chữ ký + chưa hết hạn)
        if (jwtService.isTokenValid(token)) {
            String username = jwtService.extractUsername(token);

            // 4. Chỉ set Authentication nếu CHƯA có sẵn (tránh set lại nhiều lần không cần thiết)
            if (SecurityContextHolder.getContext().getAuthentication() == null) {
                UserDetails userDetails = userDetailsService.loadUserByUsername(username);

                UsernamePasswordAuthenticationToken authToken =
                        new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
                authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                // 5. Đặt vào SecurityContext — TỪ ĐÂY, Controller/Service phía sau coi như "đã đăng nhập"
                SecurityContextHolder.getContext().setAuthentication(authToken);
            }
        }

        // 6. Luôn cho request đi tiếp — nếu token invalid, KHÔNG set Authentication
        //    → SecurityConfig (file tiếp theo) sẽ tự chặn 401 dựa trên việc route đó cần "authenticated" mà chưa có
        filterChain.doFilter(request, response);
    }
}