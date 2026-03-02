package com.example.demo.controller;

import com.example.demo.entity.PostEntity;
import com.example.demo.model.ApiResponse;
import com.example.demo.model.content_dto.CreateGroupPostRequest;
import com.example.demo.model.content_dto.CreatePostRequest;
import com.example.demo.model.content_dto.PostDetailResponse;
import com.example.demo.model.content_dto.SharePostRequest;
import com.example.demo.service.content_service.PostService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
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
    @PostMapping("/share")
    @Operation(
            summary = "Chia sẻ bài viết",
            description = """
    Chia sẻ một bài viết hiện có lên trang cá nhân của bạn hoặc vào một nhóm.

    **Quy tắc:**
    - Không thể chia sẻ bài viết ở chế độ `PRIVATE` (trừ khi bạn là chủ sở hữu)
    - Nếu bài viết mục tiêu vốn là một bài chia sẻ, hệ thống sẽ luôn liên kết tới **bài viết gốc ban đầu**
    - Chia sẻ vào nhóm yêu cầu bạn phải là **thành viên** của nhóm đó
    - Nếu nhóm có `requirePostApproval = true`, bài viết sẽ được đặt ở trạng thái `PENDING` và phải được Admin/Moderator phê duyệt trước khi hiển thị cho các thành viên khác

    **contextType:**
    - `PROFILE` → chia sẻ lên trang cá nhân của bạn, `contextId` có thể để null
    - `GROUP` → chia sẻ vào một nhóm, `contextId` = groupId (bắt buộc)
    """
    )
    public ApiResponse<?> sharePost(@RequestBody SharePostRequest sharePostRequest) {
        postService.sharePost(sharePostRequest);
        return ApiResponse.success(null);
    }
    @PostMapping("/group/{groupId}")
    @Operation(
            summary = "Tạo bài đăng trong nhóm",
            description = """
    Tạo một bài đăng mới trong một nhóm cụ thể.
    
    **Quy tắc:**
    - Người gọi phải là **thành viên** của nhóm
    - `targetType` và `targetId` là **bắt buộc** (phải đính kèm Song hoặc Album)
    - Nếu nhóm có `requirePostApproval = true` → bài đăng sẽ ở trạng thái `PENDING`
      và chỉ hiển thị với người đăng và Admin/Moderator cho đến khi được duyệt
    - Nếu `requirePostApproval = false` → bài đăng sẽ ở trạng thái `APPROVED`
      và hiển thị ngay lập tức
    """
    )
    public ApiResponse<?> createGroupPost(
            @PathVariable String groupId,
            @RequestBody @Valid CreateGroupPostRequest request) {
        postService.createGroupPost(groupId, request);
        return ApiResponse.success(null);
    }
}
