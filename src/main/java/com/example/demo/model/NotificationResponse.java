package com.example.demo.model;

import com.example.demo.model.enum_object.ActionType;
import com.example.demo.model.enum_object.TargetNotiType;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class NotificationResponse {
    private String id;
    private ActionType actionType;
    private TargetNotiType targetType;
    private String targetId;
    private Boolean isRead;
    private LocalDateTime createdAt;
    private ActorDto actor;

    @Data
    @Builder
    public static class ActorDto {
        private String id;
        private String name;
        private String imageUrl;
    }
}