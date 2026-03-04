package com.example.demo.controller;

import com.example.demo.model.ApiResponse;
import com.example.demo.model.content_dto.CommentResponse;
import com.example.demo.model.content_dto.CreateCommentRequest;
import com.example.demo.model.content_dto.UpdateCommentRequest;
import com.example.demo.service.content_service.CommentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/comments")
@RequiredArgsConstructor
public class CommentController {

    private final CommentService commentService;

    @Operation(
            summary = "Tạo bình luận hoặc phản hồi mới",
            description = "Gửi nội dung bình luận vào bài viết. Nếu có `parentCommentId`, hệ thống sẽ hiểu đây là một phản hồi (reply)."
    )
    @PostMapping
    public ApiResponse<CommentResponse> createComment(@RequestBody @Valid CreateCommentRequest request) {
        CommentResponse response = commentService.createComment(request);
        return ApiResponse.success(response);
    }

    @Operation(
            summary = "Lấy danh sách bình luận gốc của bài viết",
            description = "Trả về danh sách các bình luận cấp 1 (không phải reply) của một bài viết, hỗ trợ phân trang."
    )
    @GetMapping("/post/{postId}")
    public ApiResponse<Page<CommentResponse>> getRootComments(
            @PathVariable String postId,
            @Parameter(description = "Thông tin phân trang (page, size, sort)") Pageable pageable) {
        Page<CommentResponse> responses = commentService.getRootComments(postId, pageable);
        return ApiResponse.success(responses);
    }

    @Operation(
            summary = "Lấy danh sách phản hồi (Replies) của một bình luận",
            description = "Trả về danh sách các phản hồi thuộc về một bình luận cha cụ thể."
    )
    @GetMapping("/{commentId}/replies")
    public ApiResponse<Page<CommentResponse>> getReplies(
            @PathVariable String commentId,
            @Parameter(description = "Thông tin phân trang (page, size, sort)") Pageable pageable) {
        Page<CommentResponse> responses = commentService.getReplies(commentId, pageable);
        return ApiResponse.success(responses);
    }

    @Operation(summary = "Chỉnh sửa nội dung bình luận")
    @PutMapping("/{commentId}")
    public ApiResponse<CommentResponse> updateComment(
            @PathVariable String commentId,
            @RequestBody @Valid UpdateCommentRequest request) {
        CommentResponse response = commentService.updateComment(commentId, request);
        return ApiResponse.success(response);
    }

    @Operation(summary = "Xóa bình luận", description = "Xóa bình luận và toàn bộ các phản hồi liên quan theo cơ chế đệ quy.")
    @DeleteMapping("/{commentId}")
    public ApiResponse<Void> deleteComment(@PathVariable String commentId) {
        commentService.deleteComment(commentId);
        return ApiResponse.success(null);
    }
}
