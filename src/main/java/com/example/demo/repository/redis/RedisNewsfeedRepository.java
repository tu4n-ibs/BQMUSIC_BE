package com.example.demo.repository.redis;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Repository;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Repository
@RequiredArgsConstructor
public class RedisNewsfeedRepository {

    private final StringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper;  // Jackson — inject từ Spring context

    /**
     * Lưu danh sách postId đã được rank vào Redis Hash.
     * Hash structure:
     *   newsfeed:{userId}
     *     postIds  → "[id1, id2, id3, ...]"   (JSON)
     *     cachedAt → "2024-01-01T10:00:00"
     */
    public void saveRankedPostIds(String userId, List<String> rankedPostIds) {
        String key = RedisNewsfeedSchema.getNewsfeedKey(userId);

        try {
            Map<String, String> data = new HashMap<>();
            data.put(RedisNewsfeedSchema.FIELD_POST_IDS,  objectMapper.writeValueAsString(rankedPostIds));

            redisTemplate.opsForHash().putAll(key, data);
            redisTemplate.expire(key, RedisNewsfeedSchema.NEWSFEED_TTL_SECONDS, TimeUnit.SECONDS);
        } catch (JsonProcessingException e) {
            // Nếu lỗi serialize → bỏ qua cache, service sẽ fallback tính lại
            throw new RuntimeException("Failed to serialize postIds to Redis", e);
        }
    }

    /**
     * Lấy danh sách postId đã rank từ cache.
     * @return null nếu cache miss hoặc hết TTL
     */
    public List<String> getRankedPostIds(String userId) {
        String key = RedisNewsfeedSchema.getNewsfeedKey(userId);
        String json = (String) redisTemplate.opsForHash().get(key, RedisNewsfeedSchema.FIELD_POST_IDS);

        if (json == null) return null; 

        try {
            return objectMapper.readValue(json, new TypeReference<>() {});
        } catch (JsonProcessingException e) {
            return null; // dữ liệu lỗi → coi như cache miss
        }
    }

    /**
     * Xóa cache khi user có hành động mới
     * (follow ai đó, join group, nghe bài mới, v.v.)
     */
    public void invalidate(String userId) {
        redisTemplate.delete(RedisNewsfeedSchema.getNewsfeedKey(userId));
    }

}
