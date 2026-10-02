package com.vnguyenx.realtimechatai.dto.conversation;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UpdateNicknameRequest {

    @NotBlank(message = "Username người được đặt biệt danh không được để trống")
    private String targetUsername;

    @Size(max = 30, message = "Biệt danh không được quá 30 ký tự")
    @Pattern(
        regexp = "^[\\p{L}\\p{N} _.-]*$",
        message = "Biệt danh chỉ được chứa chữ cái, số, khoảng trắng và các ký tự _ . -"
    )
    private String nickname; // rỗng/null = xoá biệt danh
}