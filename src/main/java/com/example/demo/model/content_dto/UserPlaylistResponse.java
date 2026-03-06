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
    private String description;
    private long songCount;
    private LocalDateTime createdAt;
}