package com.example.demo.model.content_dto;

import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

@Data
public class AlbumCreateRequest {
    private String name;
    private String description;
    private MultipartFile file;
}
