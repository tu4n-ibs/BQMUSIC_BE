package com.example.demo.service.content_service;

import com.example.demo.entity.*;
import com.example.demo.model.content_dto.*;
import com.example.demo.model.enum_object.*;
import com.example.demo.repository.*;
import com.example.demo.repository.redis.RedisNewsfeedRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class NewsfeedService {

    private final PostRepository          postRepository;
    private final UserFollowRepository    userFollowRepository;
    private final GroupMemberRepository   groupMemberRepository;
    private final PlayHistoryRepository   playHistoryRepository;
    private final LikeRepository          likeRepository;
    private final CommentRepository       commentRepository;
    private final SongRepository          songRepository;
    private final AlbumRepository         albumRepository;
    private final GroupRepository         groupRepository;

    private final RedisNewsfeedRepository redisNewsfeedRepository;

    private static final int TOP_GENRE_LIMIT  = 5;
    private static final int FETCH_PER_SOURCE = 50;
    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public Slice<PostResponsePage> getPersonalizedNewsfeed(String currentUserId, Pageable pageable) {

        // ── Step 1: Lấy ranked postIds (cache HIT → dùng luôn, MISS → tính lại) ──
        List<String> rankedPostIds = getRankedPostIds(currentUserId);

        // ── Step 2: Phân trang trên danh sách ids ─────────────────────────────
        int pageNum  = pageable.getPageNumber();
        int pageSize = pageable.getPageSize();
        int fromIdx  = pageNum * pageSize;

        if (fromIdx >= rankedPostIds.size()) {
            return new SliceImpl<>(Collections.emptyList(), pageable, false);
        }

        int toIdx   = Math.min(fromIdx + pageSize, rankedPostIds.size());
        boolean hasNext = toIdx < rankedPostIds.size();
        List<String> pageIds = rankedPostIds.subList(fromIdx, toIdx);

        // ── Step 3: Fetch đúng N bài của page hiện tại ────────────────────────
        Map<String, PostEntity> postById = postRepository.findAllById(pageIds)
                .stream()
                .collect(Collectors.toMap(PostEntity::getId, p -> p));

        // Giữ đúng thứ tự rank từ Redis
        List<PostEntity> pagePosts = pageIds.stream()
                .map(postById::get)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        // ── Step 4: Batch-load enrichment (chỉ cho N bài của page) ────────────
        List<String> postIds = pagePosts.stream().map(PostEntity::getId).collect(Collectors.toList());

        Map<String, Long>    likeCountMap    = batchCountLikes(postIds);
        Map<String, Long>    commentCountMap = batchCountComments(postIds);
        Set<String>          likedPostIds    = likeRepository
                .findLikedPostIdsByUserIdAndPostIds(currentUserId, postIds);
        Map<String, SongEntity>  songMap  = batchLoadSongs(pagePosts);
        Map<String, AlbumEntity> albumMap = batchLoadAlbums(pagePosts);
        Map<String, GroupEntity> groupMap = batchLoadGroups(pagePosts);

        List<PostResponsePage> result = pagePosts.stream()
                .map(post -> mapToResponse(post, likeCountMap, commentCountMap,
                        likedPostIds, songMap, albumMap, groupMap))
                .collect(Collectors.toList());

        return new SliceImpl<>(result, pageable, hasNext);
    }

    public void invalidateNewsfeedCache(String userId) {
        redisNewsfeedRepository.invalidate(userId);
        log.debug("[Newsfeed] Cache invalidated for userId={}", userId);
    }

    private List<String> getRankedPostIds(String userId) {
        List<String> cached = redisNewsfeedRepository.getRankedPostIds(userId);
        if (cached != null) {
            log.debug("[Newsfeed] Cache HIT userId={}", userId);
            return cached;
        }

        log.debug("[Newsfeed] Cache MISS userId={} → rebuilding score...", userId);
        List<String> ranked = buildRankedPostIds(userId);
        redisNewsfeedRepository.saveRankedPostIds(userId, ranked);
        return ranked;
    }

    private static final int FALLBACK_FETCH_LIMIT = 50;

    private List<String> buildRankedPostIds(String userId) {
        Pageable sourcePage = PageRequest.of(0, FETCH_PER_SOURCE);

        List<String> followingIds = userFollowRepository.findUserFollowByFollower_Id(userId)
                .stream().map(f -> f.getFollowing().getId()).toList();
        List<String> groupIds     = groupMemberRepository.findGroupIdsByUserId(userId);
        List<String> topGenreIds  = fetchTopGenreIds(userId);

        Map<String, ScoredPost> scoreMap = new LinkedHashMap<>();

        if (!followingIds.isEmpty()) {
            mergeIntoMap(scoreMap,
                    postRepository.findPostsByFollowings(
                            followingIds, List.of(Visibility.PUBLIC),
                            ApprovalStatus.APPROVED, sourcePage),
                    ScoredPost.WEIGHT_FOLLOWING);
        }

        if (!groupIds.isEmpty()) {
            mergeIntoMap(scoreMap,
                    postRepository.findPostsByGroups(
                            ContextType.GROUP, groupIds,
                            ApprovalStatus.APPROVED, Visibility.PUBLIC, sourcePage),
                    ScoredPost.WEIGHT_GROUP);
        }

        if (!topGenreIds.isEmpty()) {
            mergeIntoMap(scoreMap,
                    postRepository.findPostsByGenres(
                            TargetType.SONG, topGenreIds,
                            Visibility.PUBLIC, ApprovalStatus.APPROVED, sourcePage),
                    ScoredPost.WEIGHT_GENRE);
        }

        // ── Fallback: user mới / chưa có dữ liệu cá nhân hóa ─────────────────
        if (scoreMap.isEmpty()) {
            log.debug("[Newsfeed] No personalized data for userId={} → using trending fallback", userId);
            return buildFallbackPostIds();
        }

        return scoreMap.values().stream()
                .peek(ScoredPost::applyRecencyBonus)
                .sorted(Comparator.comparingDouble(ScoredPost::getScore).reversed())
                .map(sp -> sp.getPost().getId())
                .collect(Collectors.toList());
    }

    private List<String> buildFallbackPostIds() {
        Pageable fallbackPage = PageRequest.of(0, FALLBACK_FETCH_LIMIT);

        // Lấy các bài PUBLIC + APPROVED mới nhất, sort by createdAt DESC
        return postRepository.findTrendingPublicPosts(
                        Visibility.PUBLIC,
                        ApprovalStatus.APPROVED,
                        fallbackPage)
                .stream()
                .map(PostEntity::getId)
                .collect(Collectors.toList());
    }

    private void mergeIntoMap(Map<String, ScoredPost> map, List<PostEntity> posts, double weight) {
        for (PostEntity post : posts) {
            map.compute(post.getId(), (id, existing) -> {
                if (existing == null) return ScoredPost.builder().post(post).score(weight).build();
                existing.addScore(weight);
                return existing;
            });
        }
    }

    private List<String> fetchTopGenreIds(String userId) {
        return playHistoryRepository
                .findTopGenreIdsByUserId(userId, PageRequest.of(0, TOP_GENRE_LIMIT))
                .stream().map(row -> (String) row[0]).collect(Collectors.toList());
    }

    private Map<String, Long> batchCountLikes(List<String> postIds) {
        return likeRepository.countLikesByPostIds(postIds).stream()
                .collect(Collectors.toMap(r -> (String) r[0], r -> (Long) r[1]));
    }

    private Map<String, Long> batchCountComments(List<String> postIds) {
        return commentRepository.countCommentsByPostIds(postIds).stream()
                .collect(Collectors.toMap(r -> (String) r[0], r -> (Long) r[1]));
    }

    private Map<String, SongEntity> batchLoadSongs(List<PostEntity> posts) {
        List<String> ids = extractTargetIds(posts, TargetType.SONG);
        if (ids.isEmpty()) return Collections.emptyMap();
        return songRepository.findAllById(ids).stream()
                .collect(Collectors.toMap(SongEntity::getId, s -> s));
    }

    private Map<String, AlbumEntity> batchLoadAlbums(List<PostEntity> posts) {
        List<String> ids = extractTargetIds(posts, TargetType.ALBUM);
        if (ids.isEmpty()) return Collections.emptyMap();
        return albumRepository.findAllById(ids).stream()
                .collect(Collectors.toMap(AlbumEntity::getId, a -> a));
    }

    private Map<String, GroupEntity> batchLoadGroups(List<PostEntity> posts) {
        Set<String> ids = new HashSet<>();
        for (PostEntity p : posts) {
            if (p.getContextType() == ContextType.GROUP && p.getContextTypeId() != null)
                ids.add(p.getContextTypeId());
            if (p.getOriginalPost() != null
                    && p.getOriginalPost().getContextType() == ContextType.GROUP
                    && p.getOriginalPost().getContextTypeId() != null)
                ids.add(p.getOriginalPost().getContextTypeId());
        }
        if (ids.isEmpty()) return Collections.emptyMap();
        return groupRepository.findAllById(ids).stream()
                .collect(Collectors.toMap(GroupEntity::getId, g -> g));
    }

    private List<String> extractTargetIds(List<PostEntity> posts, TargetType type) {
        Set<String> ids = new HashSet<>();
        for (PostEntity p : posts) {
            if (p.getTargetType() == type && p.getTargetId() != null) ids.add(p.getTargetId());
            if (p.getOriginalPost() != null
                    && p.getOriginalPost().getTargetType() == type
                    && p.getOriginalPost().getTargetId() != null)
                ids.add(p.getOriginalPost().getTargetId());
        }
        return new ArrayList<>(ids);
    }

    private PostResponsePage mapToResponse(PostEntity post,
                                           Map<String, Long> likeCountMap,
                                           Map<String, Long> commentCountMap,
                                           Set<String> likedPostIds,
                                           Map<String, SongEntity> songMap,
                                           Map<String, AlbumEntity> albumMap,
                                           Map<String, GroupEntity> groupMap) {
        PostResponsePage res = new PostResponsePage();
        UserEntity author = post.getUserEntity();

        res.setIdUser(author.getId());
        res.setImageUrlUser(author.getImageUrl());
        res.setUsername(author.getName());
        res.setIdPost(post.getId());
        res.setPostDate(post.getCreatedAt() != null ? post.getCreatedAt().toString() : null);
        res.setPostType(post.getPostType());
        res.setContextType(post.getContextType());
        res.setVisibility(post.getVisibility());
        res.setTargetType(post.getTargetType());
        res.setContent(post.getContent());
        res.setApprovalStatus(post.getApprovalStatus());
        res.setLikeCount(likeCountMap.getOrDefault(post.getId(), 0L));
        res.setCommentCount(commentCountMap.getOrDefault(post.getId(), 0L));
        res.setLiked(likedPostIds.contains(post.getId()));

        if (post.getContextType() == ContextType.GROUP) {
            GroupEntity group = groupMap.get(post.getContextTypeId());
            if (group != null) { 
                res.setGroupId(group.getId()); 
                res.setGroupName(group.getName()); 
                res.setGroupImage(group.getImageUrl());
            }
        }

        if (post.getPostType() == PostType.OWNER) {
            resolveTarget(post.getTargetType(), post.getTargetId(), res, songMap, albumMap);

        } else if (post.getPostType() == PostType.SHARE && post.getOriginalPost() != null) {
            PostEntity original     = post.getOriginalPost();
            UserEntity sharedAuthor = original.getUserEntity();

            res.setIdPostShare(original.getId());
            res.setContentShare(post.getContent());
            res.setContent(original.getOriginalContent());
            res.setTimeShare(post.getCreatedAt());
            res.setUserIdShare(sharedAuthor.getId());
            res.setUserNameShare(sharedAuthor.getName());
            res.setUserImageShare(sharedAuthor.getImageUrl());

            if (original.getContextType() == ContextType.GROUP) {
                GroupEntity og = groupMap.get(original.getContextTypeId());
                if (og != null) { res.setGroupPostIdShare(og.getId()); res.setGroupPostNameShare(og.getName()); }
            }
            resolveTarget(original.getTargetType(), original.getTargetId(), res, songMap, albumMap);
        }
        return res;
    }

    private void resolveTarget(TargetType type, String id, PostResponsePage res,
                               Map<String, SongEntity> songMap, Map<String, AlbumEntity> albumMap) {
        if (type == null || id == null) return;
        if (type == TargetType.SONG) {
            SongEntity song = songMap.get(id);
            if (song != null) {
                res.setIdSong(song.getId()); res.setNameSong(song.getName());
                res.setImageUrlSong(song.getImageUrl());
                res.setPlayCount(song.getPlayCount());
            }
        } else if (type == TargetType.ALBUM) {
            AlbumEntity album = albumMap.get(id);
            if (album != null) {
                res.setIdAlbum(album.getId()); res.setNameAlbum(album.getName());
                res.setImageUrlAlbum(album.getImageUrl());
                res.setPlayCount(0);
            }
        }
    }

    public Slice<?> search(String keyword, TypeSearch type, Pageable pageable) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return new SliceImpl<>(Collections.emptyList(), pageable, false);
        }

        return switch (type.name()) {
            case "USER" -> userRepository.findByNameContainingIgnoreCase(keyword, pageable)
                    .map(UserDTO::fromEntity); // Đã hoàn thiện case này

            case "ALBUM" -> albumRepository.findByNameContainingIgnoreCase(keyword, pageable)
                    .map(AlbumDTO::fromEntity);

            case "GROUP" -> groupRepository.findByNameContainingIgnoreCase(keyword, pageable)
                    .map(GroupDTO::fromEntity);

            default -> songRepository.findByNameContainingIgnoreCase(keyword, pageable)
                    .map(SongDTO::fromEntity);
        };
    }
}