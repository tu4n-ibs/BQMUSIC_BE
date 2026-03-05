package com.example.demo.mapper;

import com.example.demo.entity.AlbumEntity;
import com.example.demo.entity.PostEntity;
import com.example.demo.entity.SongEntity;
import com.example.demo.entity.UserEntity;
import com.example.demo.model.content_dto.PostResponsePage;
import com.example.demo.model.enum_object.ContextType;
import com.example.demo.model.enum_object.PostType;
import com.example.demo.model.enum_object.TargetType;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
public class PostMapper {

    public List<PostResponsePage> toResponseList(
            List<PostEntity> posts,
            Map<String, Long> likeCountMap,
            Map<String, Long> commentCountMap,
            Map<String, SongEntity> songMap,
            Map<String, AlbumEntity> albumMap
    ) {
        return posts.stream()
                .map(p -> toResponse(p, likeCountMap, commentCountMap, songMap, albumMap))
                .toList();
    }

    private PostResponsePage toResponse(
            PostEntity p,
            Map<String, Long> likeCountMap,
            Map<String, Long> commentCountMap,
            Map<String, SongEntity> songMap,
            Map<String, AlbumEntity> albumMap
    ) {
        PostResponsePage dto = new PostResponsePage();

        // ── Author ──
        UserEntity author = p.getUserEntity();
        dto.setIdUser(author.getId());
        dto.setImageUrlUser(author.getImageUrl());
        dto.setUsername(author.getName());

        // ── Post meta ──
        dto.setIdPost(p.getId());
        dto.setPostDate(p.getCreatedAt().toString());
        dto.setPostType(p.getPostType());
        dto.setContextType(p.getContextType());
        dto.setVisibility(p.getVisibility());
        dto.setTargetType(p.getTargetType());

        // ── Like / Comment count ──
        dto.setLikeCount(likeCountMap.getOrDefault(p.getId(), 0L));
        dto.setCommentCount(commentCountMap.getOrDefault(p.getId(), 0L));

        // ── Target: Song ──
        if (p.getTargetType() == TargetType.SONG) {
            SongEntity song = songMap.get(p.getTargetId());
            if (song != null) {
                dto.setIdSong(song.getId());
                dto.setNameSong(song.getName());
                dto.setImageUrlSong(song.getImageUrl());
            }
        }

        // ── Target: Album ──
        if (p.getTargetType() == TargetType.ALBUM) {
            AlbumEntity album = albumMap.get(p.getTargetId());
            if (album != null) {
                dto.setIdAlbum(album.getId());
                dto.setNameAlbum(album.getName());
                dto.setImageUrlAlbum(album.getImageUrl());
            }
        }

        // ── Group context ──
        if (p.getContextType() == ContextType.GROUP) {
            dto.setGroupId(p.getContextTypeId());
            // groupName sẽ được enrich ở service nếu cần
        }

        // ── Share info (originalPost) ──
        if (p.getPostType() == PostType.SHARE && p.getOriginalPost() != null) {
            PostEntity op = p.getOriginalPost();
            UserEntity sharer = op.getUserEntity();

            dto.setUserIdShare(sharer.getId());
            dto.setUserNameShare(sharer.getName());
            dto.setUserImageShare(sharer.getImageUrl());
            dto.setContentShare(op.getOriginalContent());
            dto.setTimeShare(op.getCreatedAt());

            // Group share
            if (op.getContextType() == ContextType.GROUP) {
                dto.setGroupPostIdShare(op.getContextTypeId());
            }
        }

        return dto;
    }
}