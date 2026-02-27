package com.example.demo.repository.redis;

import org.springframework.stereotype.Component;

@Component
public class RedisAuthSchema {
    public static final String FORGOT_PASSWORD_PREFIX = "forgot_password:";
    public static final String REGISTER_PREFIX = "register_session:";

    public static String getForgotPasswordKey(String email) {
        return FORGOT_PASSWORD_PREFIX + email;
    }

    public static String getRegisterKey(String email) {
        return REGISTER_PREFIX + email;
    }
}
