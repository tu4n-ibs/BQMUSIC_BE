package com.example.demo.model.content_dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class TopSongResponse {
    private int     rank;
    private String  songId;
    private String  songName;
    private String  imageUrl;
    private String  musicUrl;
    private String  artistId;
    private String  artistName;
    private String  genreId;
    private String  genreName;
    private Integer duration;
    private long    playCount;
}