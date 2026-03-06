package com.example.demo.service.content_service;

import com.example.demo.common.AppException;
import com.example.demo.common.SecurityUtils;
import com.example.demo.entity.*;
import com.example.demo.model.content_dto.*;

import com.example.demo.model.enum_object.*;
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
    private final GroupRepository groupRepository;
    private final GroupMemberRepository groupMemberRepository;
    private final LikeRepository likeRepository;
    private final CommentRepository commentRepository;
    private final NewsfeedService newsfeedService;

    public void userCreateNewPost(CreatePostRequest createPostRequest) {
        String userId = SecurityUtils.getCurrentUserId();
        UserEntity userEntity = userRepository.findById(userId)
                .orElseThrow(() -> new AppException(HttpStatus.NOT_FOUND, "USER_NF_001", "User not found"));
        validateTargetExists(createPostRequest.getTargetType(), createPostRequest.getTargetId(),userId);
        PostEntity postEntity = PostEntity.builder()
                .userEntity(userEntity)
                .contextType(ContextType.PROFILE)
                .postType(PostType.OWNER)
                .content(createPostRequest.getContent())
                .visibility(createPostRequest.getVisibility())
                .targetType(createPostRequest.getTargetType())
                .targetId(createPostRequest.getTargetId())
                .approvalStatus(ApprovalStatus.APPROVED)
                .build();
        postRepository.save(postEntity);

    }
    private void validateTargetExists(TargetType targetType, String targetId, String userId) {
        if (targetType == null || targetId == null) return;

        switch (targetType) {
            case SONG -> {
                SongEntity song = songRepository.findById(targetId)
                        .orElseThrow(() -> new AppException(HttpStatus.NOT_FOUND, "SONG_NF_001", "Song not found"));

                if (!song.getUser().getId().equals(userId)) {
                    throw new AppException(HttpStatus.FORBIDDEN, "SONG_FB_001", "You don't own this song");
                }

                song.setStatus(Status.PUBLISHED);
                songRepository.save(song);
            }
            case ALBUM -> {
                AlbumEntity album = albumRepository.findById(targetId)
                        .orElseThrow(() -> new AppException(HttpStatus.NOT_FOUND, "ALBUM_NF_001", "Album not found"));

                if (!album.getUser().getId().equals(userId)) {
                    throw new AppException(HttpStatus.FORBIDDEN, "ALBUM_FB_001", "You don't own this album");
                }
            }
        }
    }

    public void sharePost(SharePostRequest request) {
        String userId = SecurityUtils.getCurrentUserId();

        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new AppException(HttpStatus.NOT_FOUND, "USER_NF_001", "User not found"));

        // 1. Tìm bài được share
        PostEntity targetPost = postRepository.findById(request.getOriginalPostId())
                .orElseThrow(() -> new AppException(HttpStatus.NOT_FOUND, "POST_NF_001", "Post not found"));

        // 2. Nếu bài đó cũng là SHARE → trỏ về bài gốc thực sự, tránh chain vô tận
        PostEntity rootPost = (targetPost.getPostType() == PostType.SHARE && targetPost.getOriginalPost() != null)
                ? targetPost.getOriginalPost()
                : targetPost;

        // 3. Không cho share bài PRIVATE (trừ chính chủ)
        if (rootPost.getVisibility() == Visibility.PRIVATE
                && !rootPost.getUserEntity().getId().equals(userId)) {
            throw new AppException(HttpStatus.FORBIDDEN, "POST_FB_001", "This post cannot be shared");
        }

        // 4. Xử lý theo context
        ApprovalStatus approvalStatus = ApprovalStatus.APPROVED;
        String contextId ;

        if (request.getContextType() == ContextType.GROUP) {
            if (request.getContextId() == null) {
                throw new AppException(HttpStatus.BAD_REQUEST, "GROUP_BR_001", "Group id is required");
            }

            GroupEntity group = groupRepository.findById(request.getContextId())
                    .orElseThrow(() -> new AppException(HttpStatus.NOT_FOUND, "GROUP_NF_001", "Group not found"));

            // Kiểm tra user có trong group không
            boolean isMember = groupMemberRepository
                    .existsByGroupEntity_IdAndUserEntity_Id(group.getId(), userId);
            if (!isMember) {
                throw new AppException(HttpStatus.FORBIDDEN, "GROUP_FB_001", "You are not a member of this group");
            }

            // Nếu group yêu cầu duyệt → PENDING
            if (Boolean.TRUE.equals(group.getRequirePostApproval())) {
                boolean isAdmin = groupMemberRepository.findByGroupEntity_IdAndUserEntity_Id(group.getId(), userId)
                        .map(m -> m.getGroupRole() == GroupRole.ADMIN)
                        .orElse(false);
                if (!isAdmin) {
                    approvalStatus = ApprovalStatus.PENDING;
                }
            }

            contextId = group.getId();

        } else {
            // PROFILE → contextId là userId
            contextId = userId;
        }

        // 5. Tạo bài share
        PostEntity sharePost = PostEntity.builder()
                .userEntity(user)
                .contextType(request.getContextType())
                .contextTypeId(contextId)
                .postType(PostType.SHARE)
                .originalPost(rootPost)                  // luôn trỏ về bài gốc thực sự
                .content(request.getContent())           // caption của người share
                .originalContent(rootPost.getContent()) // snapshot nội dung gốc
                .visibility(request.getVisibility())
                .targetType(rootPost.getTargetType())    // kế thừa target từ bài gốc
                .targetId(rootPost.getTargetId())
                .approvalStatus(approvalStatus)
                .build();

        postRepository.save(sharePost);
    }

    public PostDetailResponse getPostDetail(String postId) {
        PostEntity post = postRepository.findById(postId)
                .orElseThrow(() -> new RuntimeException("Post not found"));

        PostEntity contentPost = (post.getPostType() == PostType.SHARE && post.getOriginalPost() != null)
                ? post.getOriginalPost()
                : post;
        long likeCount = likeRepository.countByPost_Id(postId);
        long commentCount = commentRepository.countByPost_Id(postId);
        String currentUserId = SecurityUtils.getCurrentUserId();
        boolean isLiked = false;
        if (currentUserId != null) {
            isLiked = likeRepository.existsByPost_IdAndUser_Id(postId, currentUserId);
        }
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
                .likeCount(likeCount)
                .commentCount(commentCount)
                .isLiked(isLiked)
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
    public void createGroupPost(String groupId, CreateGroupPostRequest request) {
        String userId = SecurityUtils.getCurrentUserId();

        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new AppException(HttpStatus.NOT_FOUND, "USER_NF_001", "User not found"));

        GroupEntity group = groupRepository.findById(groupId)
                .orElseThrow(() -> new AppException(HttpStatus.NOT_FOUND, "GROUP_NF_001", "Group not found"));

        // 1. Kiểm tra user có phải member của group không
        boolean isMember = groupMemberRepository.existsByGroupEntity_IdAndUserEntity_Id(groupId, userId);
        if (!isMember) {
            throw new AppException(HttpStatus.FORBIDDEN, "GROUP_FB_001", "You are not a member of this group");
        }

        // 2. Validate target tồn tại — chỉ check, không mutate
        //    (Song trong group không tự động PUBLISHED như post profile)
        validateTargetExists(request.getTargetType(), request.getTargetId());

        // 3. Xác định approvalStatus dựa vào setting của group
        ApprovalStatus approvalStatus = ApprovalStatus.APPROVED;
        if (Boolean.TRUE.equals(group.getRequirePostApproval())) {
            boolean isAdmin = groupMemberRepository.findByGroupEntity_IdAndUserEntity_Id(groupId, userId)
                    .map(m -> m.getGroupRole() == GroupRole.ADMIN)
                    .orElse(false);
            if (!isAdmin) {
                approvalStatus = ApprovalStatus.PENDING;
            }
        }

        PostEntity post = PostEntity.builder()
                .userEntity(user)
                .contextType(ContextType.GROUP)
                .contextTypeId(groupId)
                .postType(PostType.OWNER)
                .content(request.getContent())
                .visibility(request.getVisibility())
                .targetType(request.getTargetType())
                .targetId(request.getTargetId())
                .approvalStatus(approvalStatus)
                .build();

        postRepository.save(post);
    }

    // validate dùng chung — không truyền userId vì group post không check ownership song
    private void validateTargetExists(TargetType targetType, String targetId) {
        if (targetType == null || targetId == null) return;

        switch (targetType) {
            case SONG -> {
                if (!songRepository.existsById(targetId)) {
                    throw new AppException(HttpStatus.NOT_FOUND, "SONG_NF_001", "Song not found");
                }
            }
            case ALBUM -> {
                if (!albumRepository.existsById(targetId)) {
                    throw new AppException(HttpStatus.NOT_FOUND, "ALBUM_NF_001", "Album not found");
                }
            }
        }
    }
    public Page<PostResponsePage> findAllPostByUser(String userId, PostType postType, Pageable pageable) {
        String currentUserId = SecurityUtils.getCurrentUserId();
        boolean isOwner = currentUserId.equals(userId);
        Page<PostEntity> posts;
        if (isOwner) {
            posts = postRepository.findPostsByUserIdForOwner(userId, postType, pageable);
        } else {
            posts = postRepository.findPostsByUserId(
                    userId, currentUserId,
                    ContextType.PROFILE,
                    postType,
                    ApprovalStatus.APPROVED,
                    Visibility.PRIVATE,
                    pageable
            );
        }
        return posts.map(post -> {
            PostEntity contentPost = (post.getPostType() == PostType.SHARE && post.getOriginalPost() != null)
                    ? post.getOriginalPost()
                    : post;

            PostResponsePage.PostResponsePageBuilder builder = PostResponsePage.builder()
                    .idUser(post.getUserEntity().getId())
                    .imageUrlUser(post.getUserEntity().getImageUrl())
                    .username(post.getUserEntity().getName())
                    .idPost(post.getId())
                    .likeCount(likeRepository.countByPost_Id(post.getId()))
                    .commentCount(commentRepository.countByPost_Id(post.getId()))
                    .isLiked(currentUserId != null && likeRepository.existsByPost_IdAndUser_Id(post.getId(), currentUserId))
                    .postDate(post.getCreatedAt().toString())
                    .postType(post.getPostType())
                    .contextType(post.getContextType())
                    .visibility(post.getVisibility())
                    .targetType(contentPost.getTargetType());

            // Group context
            if (post.getContextType() == ContextType.GROUP && post.getContextTypeId() != null) {
                groupRepository.findById(post.getContextTypeId()).ifPresent(group -> {
                    builder.groupId(group.getId());
                    builder.groupName(group.getName());
                });
            }

            // Share info
            if (post.getPostType() == PostType.SHARE && post.getOriginalPost() != null) {
                PostEntity original = post.getOriginalPost();
                builder.userIdShare(original.getUserEntity().getId())
                        .userNameShare(original.getUserEntity().getName())
                        .userImageShare(original.getUserEntity().getImageUrl())
                        .contentShare(original.getContent())
                        .idPostShare(original.getId())
                        .timeShare(original.getCreatedAt());

                // Nếu bài gốc thuộc GROUP
                if (original.getContextType() == ContextType.GROUP && original.getContextTypeId() != null) {
                    groupRepository.findById(original.getContextTypeId()).ifPresent(group -> {
                        builder.groupPostIdShare(group.getId());
                        builder.groupPostNameShare(group.getName());
                    });
                }
            }

            // Target: SONG
            if (TargetType.SONG.equals(contentPost.getTargetType()) && contentPost.getTargetId() != null) {
                songRepository.findById(contentPost.getTargetId()).ifPresent(song -> builder.idSong(song.getId())
                        .imageUrlSong(song.getImageUrl())
                        .nameSong(song.getName()));
            }

            // Target: ALBUM
            if (TargetType.ALBUM.equals(contentPost.getTargetType()) && contentPost.getTargetId() != null) {
                albumRepository.findById(contentPost.getTargetId()).ifPresent(album -> builder.idAlbum(album.getId())
                        .imageUrlAlbum(album.getImageUrl())
                        .nameAlbum(album.getName()));
            }

            return builder.build();
        });
    }
    public Page<PostResponsePage> findAllPostByGroup(String groupId, Pageable pageable) {
        String currentUserId = SecurityUtils.getCurrentUserId();

        GroupEntity group = groupRepository.findById(groupId)
                .orElseThrow(() -> new AppException(HttpStatus.NOT_FOUND, "GROUP_NF_001", "Group not found"));

        boolean isMember = groupMemberRepository.existsByGroupEntity_IdAndUserEntity_Id(groupId, currentUserId);
        if (group.getIsPrivate() != null && group.getIsPrivate() && !isMember) {
            throw new AppException(HttpStatus.FORBIDDEN, "GROUP_FB_001", "This community is private. You must be a member to view its posts.");
        }

        Page<PostEntity> posts = postRepository.findAllByContextTypeIdAndContextTypeAndApprovalStatusOrderByCreatedAtDesc(
                groupId,
                ContextType.GROUP,
                ApprovalStatus.APPROVED,
                pageable
        );

        return posts.map(post -> {
            PostEntity contentPost = (post.getPostType() == PostType.SHARE && post.getOriginalPost() != null)
                    ? post.getOriginalPost()
                    : post;

            PostResponsePage.PostResponsePageBuilder builder = PostResponsePage.builder()
                    .idUser(post.getUserEntity().getId())
                    .imageUrlUser(post.getUserEntity().getImageUrl())
                    .username(post.getUserEntity().getName())
                    .idPost(post.getId())
                    .likeCount(likeRepository.countByPost_Id(post.getId()))
                    .commentCount(commentRepository.countByPost_Id(post.getId()))
                    .isLiked(currentUserId != null && likeRepository.existsByPost_IdAndUser_Id(post.getId(), currentUserId))
                    .postDate(post.getCreatedAt().toString())
                    .content(contentPost.getContent())
                    .postType(post.getPostType())
                    .contextType(post.getContextType())
                    .visibility(post.getVisibility())
                    .targetType(contentPost.getTargetType())
                    .groupId(group.getId())
                    .groupName(group.getName())
                    .approvalStatus(post.getApprovalStatus());

            // Share info
            if (post.getPostType() == PostType.SHARE && post.getOriginalPost() != null) {
                PostEntity original = post.getOriginalPost();
                builder.userIdShare(original.getUserEntity().getId())
                        .userNameShare(original.getUserEntity().getName())
                        .userImageShare(original.getUserEntity().getImageUrl())
                        .contentShare(original.getContent())
                        .timeShare(original.getCreatedAt());

                // Nếu bài gốc thuộc GROUP
                if (original.getContextType() == ContextType.GROUP && original.getContextTypeId() != null) {
                    groupRepository.findById(original.getContextTypeId()).ifPresent(originalGroup -> {
                        builder.groupPostIdShare(originalGroup.getId());
                        builder.groupPostNameShare(originalGroup.getName());
                    });
                }
            }

            // Target: SONG
            if (TargetType.SONG.equals(contentPost.getTargetType()) && contentPost.getTargetId() != null) {
                songRepository.findById(contentPost.getTargetId()).ifPresent(song -> builder.idSong(song.getId())
                        .imageUrlSong(song.getImageUrl())
                        .nameSong(song.getName()));
            }

            // Target: ALBUM
            if (TargetType.ALBUM.equals(contentPost.getTargetType()) && contentPost.getTargetId() != null) {
                albumRepository.findById(contentPost.getTargetId()).ifPresent(album -> builder.idAlbum(album.getId())
                        .imageUrlAlbum(album.getImageUrl())
                        .nameAlbum(album.getName()));
            }

            return builder.build();
        });
    }

    public Page<PostResponsePage> getPendingPostsByGroup(String groupId, Pageable pageable) {
        String currentUserId = SecurityUtils.getCurrentUserId();
        GroupEntity group = groupRepository.findById(groupId)
                .orElseThrow(() -> new AppException(HttpStatus.NOT_FOUND, "GROUP_NF_001", "Group not found"));

        // Chỉ Admin mới xem được pending posts
        boolean isAdmin = groupMemberRepository.findByGroupEntity_IdAndUserEntity_Id(groupId, currentUserId)
                .map(m -> m.getGroupRole() == GroupRole.ADMIN)
                .orElse(false);
        if (!isAdmin) {
            throw new AppException(HttpStatus.FORBIDDEN, "GROUP_FB_002", "Only admins can view pending posts");
        }

        Page<PostEntity> posts = postRepository.findAllByContextTypeIdAndContextTypeAndApprovalStatusOrderByCreatedAtDesc(
                groupId,
                ContextType.GROUP,
                ApprovalStatus.PENDING,
                pageable
        );

        return posts.map(this::mapToPostResponsePage);
    }

    public void reviewPost(String postId, boolean approve) {
        String currentUserId = SecurityUtils.getCurrentUserId();
        PostEntity post = postRepository.findById(postId)
                .orElseThrow(() -> new AppException(HttpStatus.NOT_FOUND, "POST_NF_001", "Post not found"));

        if (post.getContextType() != ContextType.GROUP || post.getContextTypeId() == null) {
            throw new AppException(HttpStatus.BAD_REQUEST, "POST_BR_002", "This post is not in a group context");
        }

        String groupId = post.getContextTypeId();
        boolean isAdmin = groupMemberRepository.findByGroupEntity_IdAndUserEntity_Id(groupId, currentUserId)
                .map(m -> m.getGroupRole() == GroupRole.ADMIN)
                .orElse(false);
        if (!isAdmin) {
            throw new AppException(HttpStatus.FORBIDDEN, "GROUP_FB_002", "Only admins can review posts");
        }

        if (approve) {
            post.setApprovalStatus(ApprovalStatus.APPROVED);
            postRepository.save(post);
        } else {
            postRepository.delete(post);
        }
    }

    private PostResponsePage mapToPostResponsePage(PostEntity post) {
        PostEntity contentPost = (post.getPostType() == PostType.SHARE && post.getOriginalPost() != null)
                ? post.getOriginalPost()
                : post;

        GroupEntity group = null;
        if (post.getContextType() == ContextType.GROUP && post.getContextTypeId() != null) {
            group = groupRepository.findById(post.getContextTypeId()).orElse(null);
        }

        PostResponsePage.PostResponsePageBuilder builder = PostResponsePage.builder()
                .idUser(post.getUserEntity().getId())
                .imageUrlUser(post.getUserEntity().getImageUrl())
                .username(post.getUserEntity().getName())
                .idPost(post.getId())
                .likeCount(likeRepository.countByPost_Id(post.getId()))
                .commentCount(commentRepository.countByPost_Id(post.getId()))
                .postDate(post.getCreatedAt().toString())
                .content(contentPost.getContent())
                .postType(post.getPostType())
                .contextType(post.getContextType())
                .visibility(post.getVisibility())
                .targetType(contentPost.getTargetType())
                .approvalStatus(post.getApprovalStatus())
                .isLiked(likeRepository.existsByPost_IdAndUser_Id(post.getId(), SecurityUtils.getCurrentUserId()));

        if (group != null) {
            builder.groupId(group.getId()).groupName(group.getName());
        }

        // Share info
        if (post.getPostType() == PostType.SHARE && post.getOriginalPost() != null) {
            PostEntity original = post.getOriginalPost();
            builder.userIdShare(original.getUserEntity().getId())
                    .userNameShare(original.getUserEntity().getName())
                    .userImageShare(original.getUserEntity().getImageUrl())
                    .contentShare(original.getContent())
                    .idPostShare(original.getId())
                    .timeShare(original.getCreatedAt());

            if (original.getContextType() == ContextType.GROUP && original.getContextTypeId() != null) {
                groupRepository.findById(original.getContextTypeId()).ifPresent(g -> {
                    builder.groupPostIdShare(g.getId());
                    builder.groupPostNameShare(g.getName());
                });
            }
        }

        // Target info
        if (TargetType.SONG.equals(contentPost.getTargetType()) && contentPost.getTargetId() != null) {
            songRepository.findById(contentPost.getTargetId()).ifPresent(song -> builder.idSong(song.getId())
                    .imageUrlSong(song.getImageUrl())
                    .nameSong(song.getName()));
        }

        if (TargetType.ALBUM.equals(contentPost.getTargetType()) && contentPost.getTargetId() != null) {
            albumRepository.findById(contentPost.getTargetId()).ifPresent(album -> builder.idAlbum(album.getId())
                    .imageUrlAlbum(album.getImageUrl())
                    .nameAlbum(album.getName()));
        }

        return builder.build();
    }
}