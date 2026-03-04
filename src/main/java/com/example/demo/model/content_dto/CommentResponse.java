package com.example.demo.model.content_dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CommentResponse {
    private String id;
    private String content;

    private String userId;
    private String userName;
    private String userImageUrl;

    private String postId;
    private String parentCommentId;

    private int depth;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

}