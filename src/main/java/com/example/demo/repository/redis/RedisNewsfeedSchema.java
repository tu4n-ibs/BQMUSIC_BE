package com.example.demo.repository.redis;

import org.springframework.stereotype.Component;

@Component
public class RedisNewsfeedSchema {

    // Key pattern: newsfeed:{userId}
    public static final String NEWSFEED_PREFIX = "newsfeed:";

    // Field trong Hash
    public static final String FIELD_POST_IDS  = "postIds";   // JSON array of ranked postIds

    // TTL: cache 5 phút, sau đó tự rebuild
    public static final long NEWSFEED_TTL_SECONDS = 300L;

    public static String getNewsfeedKey(String userId) {
        return NEWSFEED_PREFIX + userId;
    }
}
