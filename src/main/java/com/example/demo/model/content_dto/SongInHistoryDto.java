package com.example.demo.model.content_dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class SongInHistoryDto {
    private String id;
    private String name;
    private String imageUrl;
    private String musicUrl;
    private Integer duration;
    private LocalDateTime lastPlayedAt;

    private ArtistSummaryDto artist;
    private GenreSummaryDto genre;

    @Data
    @Builder
    public static class ArtistSummaryDto {
        private String id;
        private String name;
        private String imageUrl;
    }

    @Data
    @Builder
    public static class GenreSummaryDto {
        private String id;
        private String name;
    }
}



