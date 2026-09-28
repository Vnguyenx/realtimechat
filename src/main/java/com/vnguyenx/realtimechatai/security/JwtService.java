package com.vnguyenx.realtimechatai.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.Date;

@Service
public class JwtService {

    // Đọc secret key và thời gian hết hạn từ application.properties, không hardcode trong code
    @Value("${jwt.secret}")
    private String jwtSecret;

    @Value("${jwt.expiration}")
    private long jwtExpirationMs;

    private SecretKey getSigningKey() {
        // Chuyển chuỗi secret (String) thành SecretKey — bắt buộc phải có độ dài đủ lớn (tối thiểu 256 bit cho thuật toán HS256)
        return Keys.hmacShaKeyFor(jwtSecret.getBytes());
    }

    // Tạo token mới — gọi lúc Register/Login thành công
    public String generateToken(String username, String role) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + jwtExpirationMs);

        return Jwts.builder()
                .subject(username) // "chủ" của token — dùng để nhận diện user sau này
                .claim("role", role) // thêm thông tin role vào token
                .issuedAt(now)               // thời điểm tạo token
                .expiration(expiryDate)      // thời điểm hết hạn
                .signWith(getSigningKey())   // ký bằng secret key — đảm bảo token không bị giả mạo
                .compact();                  // build thành chuỗi String cuối cùng
    }

    // Đọc username từ token — dùng lúc verify request có token hợp lệ - cho biết user nào đang gửi request
    public String extractUsername(String token) {
        Claims claims = Jwts.parser()
                .verifyWith(getSigningKey())   // verify chữ ký trước khi đọc nội dung
                .build()
                .parseSignedClaims(token)
                .getPayload();

        return claims.getSubject();
    }

    // Kiểm tra token còn hợp lệ không (đúng chữ ký + chưa hết hạn)
    public boolean isTokenValid(String token) {
        try {
            Jwts.parser()
                    .verifyWith(getSigningKey())
                    .build()
                    .parseSignedClaims(token); // nếu sai chữ ký hoặc hết hạn, dòng này tự throw exception
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}