package com.example.demo.model.content_dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class SongResponse {
    private String id;
    private String name;
    private String artistName; // Tên người đăng (User)
    private String genreName;  // Tên thể loại
    private String musicUrl;
    private String imageUrl;
    private Integer duration;
    private Integer playCount;
}
