package com.vnguyenx.realtimechatai.dto.user;

import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data 
public class UpdateAvatarRequest {
     @Pattern(
        regexp = "^$|^https://nlbweyhylxzmvoirbzkd\\.supabase\\.co/storage/.*$",
        message = "Đường dẫn avatar không hợp lệ"
    )
    private String avatarUrl;
}
