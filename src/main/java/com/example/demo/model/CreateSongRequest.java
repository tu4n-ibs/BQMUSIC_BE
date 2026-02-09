package com.example.demo.model;

import lombok.*;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class CreateSongRequest {
    private String name;
    private String userId;
    private String musicUrl;
    private String genreId;
}
