package com.vnguyenx.realtimechatai.service;

import com.vnguyenx.realtimechatai.entity.User;
import org.springframework.stereotype.Service;

@Service
public class UserValidationService {

    public void assertUserIsActive(User user) {
        if (user.getIsBanned()) {
            throw new IllegalArgumentException("Tài khoản này đã bị khoá");
        }
        if (!user.getIsActive()) {
            throw new IllegalArgumentException("Tài khoản này không còn hoạt động");
        }
    }
}