package com.example.demo.model.content_dto;

import lombok.*;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class CreateSongRequest {
    private String name;
    private String musicUrl;
    private String genreId;
}
