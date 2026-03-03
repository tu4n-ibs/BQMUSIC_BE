package com.example.demo.model.content_dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UserPlaylistResponse {
    private String playlistId;
    private String playlistName;
    private String imageUrl; // Ảnh đại diện playlist (thường lấy của bài hát đầu tiên hoặc ảnh riêng)
    private long songCount;
    private LocalDateTime createdAt;
}