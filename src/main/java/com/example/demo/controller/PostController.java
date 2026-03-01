package com.example.demo.controller;

import com.example.demo.entity.PostEntity;
import com.example.demo.model.ApiResponse;
import com.example.demo.model.content_dto.CreatePostRequest;
import com.example.demo.model.content_dto.PostDetailResponse;
import com.example.demo.service.content_service.PostService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.persistence.Table;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/posts")
@RequiredArgsConstructor
public class PostController {

    private final PostService postService;

    @PostMapping
    public ApiResponse<?> save(@RequestBody CreatePostRequest createPostRequest) {
        postService.userCreateNewPost(createPostRequest);
        return ApiResponse.success(null);
    }

    @GetMapping("/test-find-all-post")
    public ApiResponse<Page<PostEntity>> getPosts(@ParameterObject Pageable pageable) {
        return ApiResponse.success(postService.findAllPost(pageable));
    }
    @GetMapping("post/{postId}")
    @Tag(name = "xem chi tiết post", description = "Người dùng bấm vào một Post bất kỳ")
    public ApiResponse<PostDetailResponse> postDetail(@PathVariable String postId) {
        return ApiResponse.success(postService.getPostDetail(postId));
    }
}
