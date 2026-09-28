package com.vnguyenx.realtimechatai.dto.auth;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class LoginRequest {

    @NotBlank(message = "Username không được để trống")
    @Size(min = 2, max = 10, message = "Username phải từ 2-10 ký tự")
    @Pattern(
        regexp = "^[a-zA-Z0-9]+$",
        message = "Username chỉ được chứa chữ cái và số, không dấu, không khoảng trắng"
    )
    private String username;

    @NotBlank(message = "Password không được để trống")
    @Pattern(
        regexp = "^(?=.*[0-9])(?=.*[A-Z])(?=.*[!@#$%^&*(),.?\":{}|<>])(?!.*\\s).{1,10}$",
        message = "Password không đúng định dạng"
    )
    private String password;
}