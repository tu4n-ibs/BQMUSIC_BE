package com.example.demo.controller;

import com.example.demo.model.ApiResponse;
import com.example.demo.model.content_dto.LikeResponse;
import com.example.demo.service.content_service.LikeService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/likes")
@RequiredArgsConstructor
public class LikeController {

    private final LikeService likeService;

    @Operation(
            summary = "Thả tim / Bỏ thả tim bài viết",
            description = """
Sử dụng cơ chế Toggle.
- Nếu chưa Like: Hệ thống sẽ tạo bản ghi Like mới.
- Nếu đã Like rồi: Hệ thống sẽ xóa bản ghi Like đó.
Trả về trạng thái mới nhất (`isLiked`) và tổng số lượt like hiện tại của bài viết.
"""
    )
    @PostMapping("/post/{postId}/toggle")
    public ApiResponse<LikeResponse> toggleLike(@PathVariable String postId) {
        LikeResponse response = likeService.toggleLike(postId);
        return ApiResponse.success(response);
    }

    @Operation(
            summary = "Lấy trạng thái Like của bài viết",
            description = "Dùng để hiển thị trạng thái nút Like (màu đỏ hay xám) và số lượng like khi người dùng load trang."
    )
    @GetMapping("/post/{postId}/status")
    public ApiResponse<LikeResponse> getLikeStatus(@PathVariable String postId) {
        LikeResponse response = likeService.getLikeStatus(postId);
        return ApiResponse.success(response);
    }
}