package com.example.demo.model.content_dto;

import com.example.demo.model.enum_object.ContextType;
import com.example.demo.model.enum_object.Visibility;
import lombok.Data;

@Data
public class SharePostRequest {
    private String originalPostId;  // id bài muốn share
    private String content;         // caption của người share (optional)
    private Visibility visibility;
    private ContextType contextType; // PROFILE | GROUP
    private String contextId;        // groupId nếu share vào GROUP, null nếu PROFILE
}