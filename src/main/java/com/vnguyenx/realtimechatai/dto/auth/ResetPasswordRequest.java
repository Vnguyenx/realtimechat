package com.vnguyenx.realtimechatai.dto.auth;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class ResetPasswordRequest {

    @NotBlank(message = "Token không được để trống")
    private String token;

    @NotBlank(message = "Password không được để trống")
    @Pattern(
        regexp = "^(?=.*[0-9])(?=.*[A-Z])(?=.*[!@#$%^&*(),.?\":{}|<>])(?!.*\\s).{1,10}$",
        message = "Password phải có ít nhất 1 số, 1 chữ hoa, 1 ký tự đặc biệt, không chứa khoảng trắng, tối đa 10 ký tự"
    )
    private String newPassword;

    @NotBlank(message = "Nhập lại password không được để trống")
    private String confirmNewPassword;
}