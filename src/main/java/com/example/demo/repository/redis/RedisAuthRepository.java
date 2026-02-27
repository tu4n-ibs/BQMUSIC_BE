package com.example.demo.repository.redis;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Repository;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Repository
@RequiredArgsConstructor
public class RedisAuthRepository {
    private final StringRedisTemplate redisTemplate;

    public void saveOtpSession(String key, String otp, long timeoutSeconds) {
        Map<String, String> data = new HashMap<>();
        data.put("otp", otp);
        data.put("status", "PENDING");

        redisTemplate.opsForHash().putAll(key, data);
        redisTemplate.expire(key, timeoutSeconds, TimeUnit.SECONDS);
    }

    public String getStatus(String key) {
        return (String) redisTemplate.opsForHash().get(key, "status");
    }

    public String getOtp(String key) {
        return (String) redisTemplate.opsForHash().get(key, "otp");
    }

    public void verifySession(String key, long timeoutMinutes) {
        redisTemplate.opsForHash().put(key, "status", "VERIFIED");
        redisTemplate.opsForHash().delete(key, "otp"); // Xóa OTP cho bảo mật
        redisTemplate.expire(key, timeoutMinutes, TimeUnit.MINUTES);
    }

    public boolean isExists(String key) {
        return Boolean.TRUE.equals(redisTemplate.hasKey(key));
    }

    public void delete(String key) {
        redisTemplate.delete(key);
    }
}
