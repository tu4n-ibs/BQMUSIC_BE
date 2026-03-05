package com.example.demo.model.content_dto;

import lombok.Builder;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Builder
public class GroupDetailResponse {
    private String id;
    private String name;
    private String description;
    private String imageUrl;
    private Boolean isPrivate;
    private Boolean requirePostApproval;
    private Long memberCount;
    private LocalDateTime createdAt;
    
    // Membership status for the current user
    private Boolean isMember;
    private String role; // ADMIN, MEMBER, or null
    private Boolean hasPendingRequest;
}
