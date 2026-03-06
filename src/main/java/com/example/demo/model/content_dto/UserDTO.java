package com.example.demo.model.content_dto;

import com.example.demo.entity.UserEntity;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class UserDTO {
    private String id;
    private String name;
    private String email;
    private String imageUrl;

    public static UserDTO fromEntity(UserEntity entity) {
        if (entity == null) return null;

        return UserDTO.builder()
                .id(entity.getId())
                .name(entity.getName())
                .email(entity.getEmail())
                .imageUrl(entity.getImageUrl())
                .build();
    }
}
