package com.example.demo.service.content_service;

import com.example.demo.common.AppException;
import com.example.demo.common.SecurityUtils;
import com.example.demo.entity.AlbumSongEntity;
import com.example.demo.entity.PostEntity;
import com.example.demo.entity.UserEntity;
import com.example.demo.model.content_dto.*;

import com.example.demo.model.enum_object.ContextType;
import com.example.demo.model.enum_object.PostType;
import com.example.demo.model.enum_object.TargetType;
import com.example.demo.model.enum_object.Visibility;
import com.example.demo.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PostService {

    private final PostRepository postRepository;
    private final UserRepository userRepository;
    private final SongRepository songRepository;
    private final AlbumRepository albumRepository;
    private final AlbumSongRepository albumSongRepository;

    public Page<PostEntity> findAllPost(Pageable pageable) {
        return postRepository.findAll(pageable);
    }
    public void userCreateNewPost(CreatePostRequest createPostRequest) {
        String userId = SecurityUtils.getCurrentUserId();
        UserEntity userEntity = userRepository.findById(userId)
                .orElseThrow(() -> new AppException(HttpStatus.NOT_FOUND, "USER_NF_001", "User not found"));

        validateTargetExists(createPostRequest.getTargetType(), createPostRequest.getTargetId());

        ContextType contextType = ContextType.PROFILE;
        PostType postType = PostType.OWNER;
        String content = createPostRequest.getContent();
        Visibility visibility = createPostRequest.getVisibility();
        TargetType targetType = createPostRequest.getTargetType();
        String targetId = createPostRequest.getTargetId();

        // Khởi tạo Entity - Đảm bảo constructor của bạn khớp với thứ tự này
        PostEntity postEntity = new PostEntity(
                userEntity,
                contextType,
                null,
                postType,
                null,
                content,
                null,
                visibility,
                targetType,
                targetId
        );

        postRepository.save(postEntity);
    }

    private void validateTargetExists(TargetType targetType, String targetId) {
        if (targetType == null || targetId == null) {
            return; // Hoặc quăng lỗi nếu bắt buộc phải có target
        }

        if (targetType == TargetType.SONG) {
            if (!songRepository.existsById(targetId)) {
                throw new AppException(HttpStatus.NOT_FOUND, "SONG_NF_001", "Song not found with id: " + targetId);
            }
        } else if (targetType == TargetType.ALBUM) {
            if (!albumRepository.existsById(targetId)) {
                throw new AppException(HttpStatus.NOT_FOUND, "ALBUM_NF_001", "Album not found with id: " + targetId);
            }
        }
    }

    public PostDetailResponse getPostDetail(String postId) {
        PostEntity post = postRepository.findById(postId)
                .orElseThrow(() -> new RuntimeException("Post not found"));

        PostEntity contentPost = (post.getPostType() == PostType.SHARE && post.getOriginalPost() != null)
                ? post.getOriginalPost()
                : post;

        PostDetailResponse.PostDetailResponseBuilder builder = PostDetailResponse.builder()
                .userId(post.getUserEntity().getId())
                .userName(post.getUserEntity().getName())
                .userImage(post.getUserEntity().getImageUrl())
                .content(post.getContent())
                .contextType(post.getContextType())
                .timeCreated(post.getCreatedAt()) // BaseEntity thường có createdAt
                .postType(post.getPostType())
                .visibility(post.getVisibility())
                .targetType(contentPost.getTargetType())
                .targetId(contentPost.getTargetId());

        // 3. Mapping thông tin Share (nếu có)
        if (post.getPostType() == PostType.SHARE && post.getOriginalPost() != null) {
            PostEntity original = post.getOriginalPost();
            builder.userIdShare(original.getUserEntity().getId())
                    .userNameShare(original.getUserEntity().getName())
                    .userImageShare(original.getUserEntity().getImageUrl())
                    .contentShare(original.getContent())
                    .timeShare(original.getCreatedAt());
            // Nếu post gốc nằm trong Group, bạn có thể bổ sung groupPostIdShare tại đây
        }

        // 4. Mapping Target (Song hoặc Album)
        if (TargetType.SONG.equals(contentPost.getTargetType())) {
            songRepository.findById(contentPost.getTargetId()).ifPresent(song -> builder.songName(song.getName())
                    .songImgUrl(song.getImageUrl())
                    .songUrl(song.getMusicUrl())
                    .songView(song.getPlayCount() != null ? song.getPlayCount() : 0));
        } else if (TargetType.ALBUM.equals(contentPost.getTargetType())) {
            // Giả sử bạn đã inject AlbumRepository
            albumRepository.findById(contentPost.getTargetId()).ifPresent(album -> {
                // Lấy danh sách từ repository trước khi stream
                List<AlbumSongEntity> albumSongEntities = albumSongRepository.findByAlbumEntity_Id(album.getId());

                // Mapping sang List DTO
                List<AlbumResponseForPost.SongResponseAlbum> songDtos = albumSongEntities.stream()
                        .map(as -> AlbumResponseForPost.SongResponseAlbum.builder()
                                .songId(as.getSongEntity().getId())
                                .name(as.getSongEntity().getName())
                                .duration((as.getSongEntity().getDuration())) // Hàm format bên dưới
                                .build())
                        .collect(Collectors.toList());

                // Đưa vào builder chính của PostDetailResponse
                builder.postResponse(AlbumResponseForPost.builder()
                        .name(album.getName())
                        .imageUrl(album.getImageUrl())
                        .description(album.getDescription())
                        .songs(songDtos)
                        .build());
            });
        }
        return builder.build();
    }
}