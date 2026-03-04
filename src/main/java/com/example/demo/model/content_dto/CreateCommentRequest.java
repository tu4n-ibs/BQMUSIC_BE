package com.example.demo.model.content_dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateCommentRequest {
    @NotBlank(message = "Content cannot be empty")
    private String content;

    @NotNull(message = "Post ID is required")
    private String postId;

    // Nếu là comment gốc thì để null, nếu là reply thì truyền ID comment cha
    private String parentCommentId;
}

