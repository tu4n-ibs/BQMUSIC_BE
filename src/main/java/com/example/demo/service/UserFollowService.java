package com.example.demo.service;

import com.example.demo.common.AppException;
import com.example.demo.entity.UserEntity;
import com.example.demo.entity.UserFollowEntity;
import com.example.demo.model.content_dto.UserProfileStatsResponse;
import com.example.demo.repository.PostRepository;
import com.example.demo.repository.UserFollowRepository;
import com.example.demo.repository.UserRepository;
import com.example.demo.model.enum_object.PostType;
import com.example.demo.model.enum_object.ContextType;
import com.example.demo.model.enum_object.ApprovalStatus;
import com.example.demo.service.content_service.NewsfeedService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserFollowService {

    private final UserRepository userRepository;
    private final UserFollowRepository userFollowRepository;
    private final PostRepository postRepository;
    private final NewsfeedService newsfeedService;

    @Transactional
    public void followUser(String followerId, String followingId) {
        if (followerId.equals(followingId)) {
            throw new AppException(HttpStatus.BAD_REQUEST, "FOLLOW_SELF_ERR", "Cannot follow yourself");
        }

        // 2. Tìm User trong DB
        UserEntity follower = userRepository.findById(followerId)
                .orElseThrow(() -> new AppException(HttpStatus.NOT_FOUND, "USER_NF_001", "Follower user not found"));

        UserEntity following = userRepository.findById(followingId)
                .orElseThrow(() -> new AppException(HttpStatus.NOT_FOUND, "USER_NF_002", "User to follow not found"));

        if (userFollowRepository.existsByFollowerAndFollowing(follower, following)) {
            throw new AppException(HttpStatus.BAD_REQUEST, "FOLLOW_EXIST_ERR", "You are already following this user");
        }

        // 4. Lưu vào DB
        UserFollowEntity userFollow = new UserFollowEntity();
        userFollow.setFollower(follower);
        userFollow.setFollowing(following);

        userFollowRepository.save(userFollow);
        newsfeedService.invalidateNewsfeedCache(followerId);
    }

    @Transactional
    public void unfollowUser(String followerId, String followingId) {
        UserEntity follower = userRepository.findById(followerId)
                .orElseThrow(() -> new AppException(HttpStatus.NOT_FOUND, "USER_NF_001", "Follower user not found"));

        UserEntity following = userRepository.findById(followingId)
                .orElseThrow(() -> new AppException(HttpStatus.NOT_FOUND, "USER_NF_002", "User to unfollow not found"));

        // Tìm record để xóa
        UserFollowEntity userFollow = userFollowRepository.findByFollowerAndFollowing(follower, following)
                .orElseThrow(() -> new AppException(HttpStatus.NOT_FOUND, "FOLLOW_RELATION_NF", "Relationship not found (You haven't followed this user yet)"));

        userFollowRepository.delete(userFollow);
        newsfeedService.invalidateNewsfeedCache(followerId);
    }
    public UserProfileStatsResponse getProfileStats(String targetUserId, String currentUserId) {
        // 1. Kiểm tra user có tồn tại không
        if (!userRepository.existsById(targetUserId)) {
            throw new AppException(HttpStatus.NOT_FOUND, "USER_NF_001", "User not found");
        }

        // 2. Lấy các thông số đếm (Chỉ tính các bài đăng trên PROFILE và đã APPROVED)
        long postCount = postRepository.countRegularPostsByUserId(targetUserId, ApprovalStatus.APPROVED);
        long albumCount = postRepository.countAlbumsByUserId(targetUserId, ApprovalStatus.APPROVED);
        long followers = userFollowRepository.countByFollowing_Id(targetUserId);
        long following = userFollowRepository.countByFollower_Id(targetUserId);

        // 3. Kiểm tra trạng thái follow (nếu người xem khác chủ profile)
        boolean isFollowing = false;
        if (currentUserId != null && !currentUserId.equals(targetUserId)) {
            isFollowing = userFollowRepository.existsByFollower_IdAndFollowing_Id(currentUserId, targetUserId);
        }

        return UserProfileStatsResponse.builder()
                .postCount(postCount)
                .albumCount(albumCount)
                .followerCount(followers)
                .followingCount(following)
                .isFollowing(isFollowing)
                .build();
    }
}