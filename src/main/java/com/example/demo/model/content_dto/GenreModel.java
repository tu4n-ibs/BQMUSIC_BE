package com.example.demo.model.content_dto;

import lombok.*;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class GenreModel {
    private String name;
    private String description;
    private String id;
}
