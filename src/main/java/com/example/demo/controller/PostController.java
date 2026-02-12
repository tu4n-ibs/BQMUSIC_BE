package com.example.demo.controller;

import com.example.demo.model.ApiResponse;
import com.example.demo.model.user.CreatePostRequest;
import com.example.demo.service.content_service.PostService;
import lombok.RequiredArgsConstructor;
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

}
