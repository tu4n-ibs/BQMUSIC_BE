package com.example.demo.model.content_dto;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateGroupRequest {

    private String name;

    private String description;

    private String imageUrl;

    private Boolean isPrivate;

    private Boolean requirePostApproval;
}
