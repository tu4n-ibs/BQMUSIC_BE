package com.example.demo.model.content_dto;

import com.example.demo.entity.AlbumEntity;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AlbumDTO {
    private String id;
    private String name;
    private String description;
    private String imageUrl;
    private String userId;

    public static AlbumDTO fromEntity(AlbumEntity entity) {
        if (entity == null) return null;
        return AlbumDTO.builder()
                .id(entity.getId())
                .name(entity.getName())
                .description(entity.getDescription())
                .imageUrl(entity.getImageUrl())
                .userId(entity.getUser() != null ? entity.getUser().getId() : null)
                .build();
    }
}