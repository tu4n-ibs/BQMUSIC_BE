package com.example.demo.model.content_dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class GroupJoinRequestResponse {
    private String groupJoinRequestId;
    private String userId;
    private String name;
    private String imageUrl;
    private LocalDateTime joinDate;
}
