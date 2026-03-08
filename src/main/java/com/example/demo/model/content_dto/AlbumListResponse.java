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
public class AlbumListResponse {
    private String id;
    private String name;
    private String description;
    private String imageUrl;
    private String albumImageUrl;
    private Long songCount;
    private String userId;
    private String username;
    private String nameUser;
    private LocalDateTime createdAt;
}
