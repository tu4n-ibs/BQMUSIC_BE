package com.example.demo.model;

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
