package com.example.demo.model.user;


import com.example.demo.model.enum_object.TargetType;
import com.example.demo.model.enum_object.Visibility;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class CreatePostRequest {
    private String userId;

    private String content;

    private Visibility visibility;

    private TargetType targetType;

    private String targetId;
}
