package com.example.demo.model.content_dto;

import com.example.demo.model.enum_object.TargetType;
import com.example.demo.model.enum_object.Visibility;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CreateGroupPostRequest {
    @NotNull(message = "Content is required")
    private String content;

    @NotNull(message = "Visibility is required")
    private Visibility visibility;

    @NotNull(message = "Target type is required")
    private TargetType targetType;       // SONG | ALBUM — bắt buộc

    @NotNull(message = "Target id is required")
    private String targetId;             // bắt buộc
}
