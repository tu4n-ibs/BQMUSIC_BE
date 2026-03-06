package com.example.demo.model.content_dto;

import com.example.demo.model.enum_object.ContextType;
import com.example.demo.model.enum_object.PostType;
import com.example.demo.model.enum_object.TargetType;
import com.example.demo.model.enum_object.Visibility;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class PostDetailResponse {
    private String userId;
    private String userName;
    private String userImage;
    private String content;
    private ContextType contextType; //GROUP, PROFILE
    private String groupPostId;
    private String groupPostName;
    private LocalDateTime timeCreated;
    private PostType postType; //    SHARE, OWNER
    private String userIdShare;
    private String userNameShare;
    private String userImageShare;
    private String groupPostIdShare;
    private String groupPostNameShare;
    private String contentShare;
    private LocalDateTime timeShare;
    private Visibility visibility; //     PRIVATE,PUBLIC;
    private TargetType targetType; //    SONG,ALBUM
    private String targetId;
    private AlbumResponseForPost postResponse;
    private String songImgUrl;
    private String songName;
    private String songUrl;
    private Integer playCount;
    private long likeCount;
    private long commentCount;
    private boolean isLiked;
}

