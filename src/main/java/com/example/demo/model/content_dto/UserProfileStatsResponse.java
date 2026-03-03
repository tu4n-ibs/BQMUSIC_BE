package com.example.demo.model.content_dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class UserProfileStatsResponse {
    private long postCount;
    private long followerCount;
    private long followingCount;
    private boolean isFollowing;
}