package com.example.demo.model.content_dto;

import lombok.Data;

@Data
public class AlbumCreateRequest {
    private String name;
    private String description;
    private String imageUrl;
}
