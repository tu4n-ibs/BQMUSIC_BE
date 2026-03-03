package com.example.demo.model.content_dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class GroupByUser {
    private String groupName;
    private String groupId;
    private String groupImageUrl;
}
