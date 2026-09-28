package com.vnguyenx.realtimechatai.dto.auth;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class RegisterRequest {

    @NotBlank(message = "Username không được để trống")
    @Size(min = 2, max = 10, message = "Username phải từ 2-10 ký tự")
    @Pattern(
        regexp = "^[a-zA-Z0-9]+$",
        message = "Username chỉ được chứa chữ cái và số, không dấu, không khoảng trắng"
    )
    private String username;

    @NotBlank(message = "Email không được để trống")
    @jakarta.validation.constraints.Email(message = "Email không đúng định dạng")
    private String email;

    @NotBlank(message = "Password không được để trống")
    @Pattern(
        regexp = "^(?=.*[0-9])(?=.*[A-Z])(?=.*[!@#$%^&*(),.?\":{}|<>])(?!.*\\s).{1,10}$",
        message = "Password phải có ít nhất 1 số, 1 chữ hoa, 1 ký tự đặc biệt, không chứa khoảng trắng, tối đa 10 ký tự"
    )
    private String password;

    @NotBlank(message = "Nhập lại password không được để trống")
    private String rePassword;
}