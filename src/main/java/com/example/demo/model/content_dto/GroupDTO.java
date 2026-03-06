package com.example.demo.model.content_dto;

import com.example.demo.entity.GroupEntity;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class GroupDTO {
    private String id;
    private String name;
    private String description;
    private String imageUrl;
    private Boolean isPrivate;
    private Boolean requirePostApproval;

    public static GroupDTO fromEntity(GroupEntity entity) {
        if (entity == null) return null;
        return GroupDTO.builder()
                .id(entity.getId())
                .name(entity.getName())
                .description(entity.getDescription())
                .imageUrl(entity.getImageUrl())
                .isPrivate(entity.getIsPrivate())
                .requirePostApproval(entity.getRequirePostApproval())
                .build();
    }
}