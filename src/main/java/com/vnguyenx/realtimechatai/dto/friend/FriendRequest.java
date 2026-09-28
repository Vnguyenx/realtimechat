package com.vnguyenx.realtimechatai.dto.friend;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class FriendRequest {

    @NotBlank(message = "Username người nhận không được để trống")
    @Size(min = 2, max = 10, message = "Username phải từ 2-10 ký tự")
    private String targetUsername;
}