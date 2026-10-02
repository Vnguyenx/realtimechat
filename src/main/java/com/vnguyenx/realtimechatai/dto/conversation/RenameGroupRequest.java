package com.vnguyenx.realtimechatai.dto.conversation;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class RenameGroupRequest {

    @NotBlank(message = "Tên nhóm không được để trống")
    @Size(max = 100, message = "Tên nhóm không được quá 100 ký tự")
    private String name;
}