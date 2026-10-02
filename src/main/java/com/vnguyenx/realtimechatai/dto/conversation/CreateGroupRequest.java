package com.vnguyenx.realtimechatai.dto.conversation;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;

@Data
public class CreateGroupRequest {

    @NotBlank(message = "Tên nhóm không được để trống")
    @Size(max = 100, message = "Tên nhóm không được quá 100 ký tự")
    private String name;

    @NotEmpty(message = "Nhóm cần ít nhất 1 thành viên khác ngoài bạn")
    private List<@NotBlank String> memberUsernames;
}