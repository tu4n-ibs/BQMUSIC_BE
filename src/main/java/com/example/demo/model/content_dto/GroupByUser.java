package com.example.demo.model.content_dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class GroupByUser {
    private String id;
    private String name;
    private String imageUrl;
    private String description;
    private Long members;
    private java.util.List<String> memberAvatars;
}
