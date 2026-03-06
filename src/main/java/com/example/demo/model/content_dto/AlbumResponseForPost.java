package com.example.demo.model.content_dto;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class AlbumResponseForPost {
    private String name;
    private String imageUrl;
    private String description;
    private List<SongResponseAlbum> songs;
    @Data
    @Builder
    public static class SongResponseAlbum {
        private String songId;
        private String name;
        private Integer duration;
        private String musicUrl;
    }
}
