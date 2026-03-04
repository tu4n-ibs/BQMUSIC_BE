package com.example.demo.model.content_dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

// Dùng để cập nhật nội dung comment
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateCommentRequest {
    @NotBlank(message = "Content cannot be empty")
    private String content;
}
