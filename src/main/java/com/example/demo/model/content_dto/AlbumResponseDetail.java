package com.example.demo.model.content_dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AlbumResponseDetail {
    private String albumImageUrl;
    private String name;
    private String imageUrl;
    private String description;
    private String nameUser;
    private String username;
    private String userId;
    private java.time.LocalDateTime createdAt;
    private List<SongResponseFromAlbum> songs;

    @Data
    @Builder
    public static class SongResponseFromAlbum {
        private String songId;
        private String songImageUrl;
        private String songName;
        private Integer duration;
    }
}