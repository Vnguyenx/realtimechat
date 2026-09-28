package com.vnguyenx.realtimechatai.config;

import com.vnguyenx.realtimechatai.security.CustomUserDetailsService;
import com.vnguyenx.realtimechatai.security.JwtAuthFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final CustomUserDetailsService userDetailsService;
    private final JwtAuthFilter jwtAuthFilter;

    public SecurityConfig(CustomUserDetailsService userDetailsService, JwtAuthFilter jwtAuthFilter) {
        this.userDetailsService = userDetailsService;
        this.jwtAuthFilter = jwtAuthFilter;
    }

    // Bean 1: Đóng vai trò "công cụ hash password" — AuthService đã dùng bean này ở phần register()
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    // Bean 2: Nối UserDetailsService + PasswordEncoder lại thành 1 "quy trình xác thực" hoàn chỉnh
    @Bean
    public DaoAuthenticationProvider authenticationProvider() {
    // Bản Spring Security mới: bắt buộc truyền UserDetailsService qua constructor,
    // không còn setUserDetailsService() nữa
    DaoAuthenticationProvider provider = new DaoAuthenticationProvider(userDetailsService);
    provider.setPasswordEncoder(passwordEncoder()); // setPasswordEncoder vẫn còn, chỉ setUserDetailsService bị bỏ
    return provider;
    }

    // Bean 3: AuthService đang cần bean này để gọi authenticationManager.authenticate(...)
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    // Bean 4: Quan trọng nhất — khai báo route nào public, route nào cần login, và ráp JwtAuthFilter vào
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable()) // API JWT không dùng cookie, không cần CSRF protection

            .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            // báo Spring: KHÔNG tạo session, đúng tinh thần JWT (server không lưu trạng thái)

            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/api/auth/**").permitAll()
                .requestMatchers("/api/users/invite/**").permitAll()
                .requestMatchers("/ws-chat/**").permitAll()
                .requestMatchers("/api/admin/**").hasRole("ADMIN")  // chỉ role ADMIN mới vào được nhóm route này
                .anyRequest().authenticated()                        // còn lại chỉ cần đã login (customer hay admin đều vào được)
            )
            .authenticationProvider(authenticationProvider()) // gắn "quy trình xác thực" đã khai ở Bean 2

            // Chèn JwtAuthFilter vào TRƯỚC filter mặc định của Spring Security
            // (để JwtAuthFilter chạy trước, kịp "ghi vào sổ trực" trước khi Spring check "đã login chưa")
            .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}