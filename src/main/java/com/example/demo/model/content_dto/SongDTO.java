package com.example.demo.model.content_dto;

import com.example.demo.entity.SongEntity;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class SongDTO {
    private String id;
    private String name;
    private String imageUrl;
    private String musicUrl;
    private Integer playCount;
    private String status;
    private Integer duration;

    // Lấy thông tin rút gọn của các quan hệ
    private String userId;
    private String genreId;
    private String groupName;

    public static SongDTO fromEntity(SongEntity entity) {
        if (entity == null) return null;
        return SongDTO.builder()
                .id(entity.getId())
                .name(entity.getName())
                .imageUrl(entity.getImageUrl())
                .musicUrl(entity.getMusicUrl())
                .playCount(entity.getPlayCount())
                .status(entity.getStatus() != null ? entity.getStatus().name() : null)
                .duration(entity.getDuration())
                .userId(entity.getUser() != null ? entity.getUser().getId() : null)
                .genreId(entity.getGenre() != null ? entity.getGenre().getId() : null)
                .groupName(entity.getGroup() != null ? entity.getGroup().getName() : null)
                .build();
    }
}