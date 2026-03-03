package com.example.demo.controller;

import com.example.demo.common.SecurityUtils;
import com.example.demo.model.ApiResponse;
import com.example.demo.model.content_dto.UserProfileStatsResponse;
import com.example.demo.service.UserFollowService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/follow-user")
@RequiredArgsConstructor
public class UserFollowController {

    private final UserFollowService userFollowService;

    @PostMapping("/{targetId}/follow")
    @Operation(
            summary = "Theo dõi (Follow) người dùng",
            description = """
            Thực hiện theo dõi một người dùng khác.
            
            **Quy tắc:**
            - Không thể tự theo dõi chính mình.
            - Không thể thao tác nếu người dùng mục tiêu không tồn tại.
            - Sẽ báo lỗi (Bad Request) nếu đã theo dõi người dùng này từ trước.
            """
    )
    public ApiResponse<?> followUser(@PathVariable String targetId) {
        String currentUserId = SecurityUtils.getCurrentUserId();
        userFollowService.followUser(currentUserId, targetId);
        return ApiResponse.success(null);
    }

    // ==================== HỦY THEO DÕI ====================
    @DeleteMapping("/{targetId}/unfollow")
    @Operation(
            summary = "Hủy theo dõi (Unfollow) người dùng",
            description = """
            Hủy theo dõi một người dùng đã theo dõi trước đó.
            
            **Quy tắc:**
            - Sẽ báo lỗi (Not Found) nếu giữa 2 người chưa tồn tại trạng thái theo dõi.
            - Không thể thao tác nếu người dùng mục tiêu không tồn tại.
            """
    )
    public ApiResponse<?> unfollowUser(@PathVariable String targetId) {
        String currentUserId = SecurityUtils.getCurrentUserId();
        userFollowService.unfollowUser(currentUserId, targetId);
        return ApiResponse.success(null);
    }

    // ==================== THỐNG KÊ HỒ SƠ ====================
    @GetMapping("/{userId}/stats")
    @Operation(
            summary = "Lấy thống kê hồ sơ người dùng",
            description = """
            Lấy các chỉ số thống kê tổng quan của một hồ sơ người dùng.
            
            **Thông tin trả về bao gồm:**
            - `postCount`: Tổng số bài viết của người dùng.
            - `followerCount`: Tổng số người đang theo dõi người dùng này.
            - `followingCount`: Tổng số người mà người dùng này đang theo dõi.
            - `isFollowing`: Trạng thái cho biết người gọi API (current user) có đang theo dõi hồ sơ này hay không.
            """
    )
    public ApiResponse<UserProfileStatsResponse> getUserStats(@PathVariable String userId) {
        String currentUserId = SecurityUtils.getCurrentUserId();
        UserProfileStatsResponse stats = userFollowService.getProfileStats(userId, currentUserId);

        return ApiResponse.success(stats);
    }
}