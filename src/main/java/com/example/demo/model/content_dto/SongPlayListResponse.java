package com.example.demo.model.content_dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class SongPlayListResponse {
    private String songId;
    private String songName;
    private String songImage;
    private String songArtistId;
    private String songArtistName;
    private String songAlbum;
    private String albumName;
    private long songCount;
    private String playlistName;
    private String playlistId;
}
