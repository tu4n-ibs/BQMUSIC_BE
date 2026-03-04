package com.example.demo.model.content_dto;

import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class LikeResponse {
    private String postId;
    private boolean isLiked;
    private long likeCount;
}
