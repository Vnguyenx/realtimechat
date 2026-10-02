package com.vnguyenx.realtimechatai.dto.conversation;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class AddGroupMemberRequest {

    @NotBlank(message = "Username không được để trống")
    private String targetUsername;
}