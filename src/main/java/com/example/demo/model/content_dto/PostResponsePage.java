package com.example.demo.model.content_dto;

import com.example.demo.model.enum_object.ContextType;
import com.example.demo.model.enum_object.PostType;
import com.example.demo.model.enum_object.TargetType;
import com.example.demo.model.enum_object.Visibility;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PostResponsePage {
    private String idUser;
    private String imageUrlUser;
    private String username;
    private String idPost;
    private Long likeCount;
    private Long commentCount;
    private String postDate;
    private String idSong;
    private String imageUrlSong;
    private String nameSong;
    private PostType postType; //    SHARE, OWNER
    private ContextType contextType; //GROUP, PROFILE
    private Visibility visibility; //     FRIEND,PRIVATE,PUBLIC;
    private TargetType targetType; //    SONG,ALBUM
    private String idAlbum;
    private String imageUrlAlbum;
    private String nameAlbum;
    private String groupId;
    private String groupName;
    private String userIdShare;
    private String userNameShare;
    private String userImageShare;
    private String groupPostIdShare;
    private String groupPostNameShare;
    private String contentShare;
    private LocalDateTime timeShare;
}
