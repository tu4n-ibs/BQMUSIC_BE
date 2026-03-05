package com.example.demo.controller;

import com.example.demo.model.ApiResponse;
import com.example.demo.model.content_dto.*;
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
    @Operation(
            summary = "Lấy danh sách bài viết của một người dùng (Phân trang)",
            description = """
Lấy tất cả bài viết mà một User đã đăng (bao gồm cả bài Share).
Dữ liệu trả về dưới dạng Page để tối ưu hiệu năng.

**Tham số phân trang:**
- `page`: Số trang (bắt đầu từ 0)
- `size`: Số lượng bài mỗi trang (mặc định 10)
- `sort`: Sắp xếp (ví dụ: `createdAt,desc`)
"""
    )
    @GetMapping("/user/{userId}")
    public ApiResponse<Page<PostResponsePage>> getAllPostsByUser(
            @PathVariable String userId,
            @RequestParam(required = false) com.example.demo.model.enum_object.PostType postType,
            @ParameterObject Pageable pageable
    ) {
        // Gọi service xử lý logic lấy post và map song/album
        Page<PostResponsePage> posts = postService.findAllPostByUser(userId, postType, pageable);

        return ApiResponse.success(posts);
    }
    @Operation(
            summary = "Lấy danh sách bài viết trong một nhóm (Phân trang)",
            description = """
Lấy toàn bộ bài viết đã được duyệt (`APPROVED`) trong một nhóm cụ thể.

**Quy tắc:**
- Người gọi phải là **thành viên** của nhóm (đã join).
- Nếu không phải thành viên -> Trả về lỗi `403 Forbidden`.
- Nếu nhóm không tồn tại -> Trả về lỗi `404 Not Found`.
- Trả về đầy đủ thông tin: Người đăng, số Like, số Comment, thông tin bài gốc (nếu là bài Share), và dữ liệu Song/Album đi kèm.
"""
    )
    @GetMapping("group/{groupId}")
    public ApiResponse<Page<PostResponsePage>> getPostsByGroup(
            @PathVariable String groupId,
            @ParameterObject Pageable pageable
    ) {
        // Service đã tự lấy currentUserId qua SecurityUtils để check isMember
        Page<PostResponsePage> posts = postService.findAllPostByGroup(groupId, pageable);

        return ApiResponse.success(posts);
    }
    @GetMapping("group/{groupId}/pending")
    @Operation(
            summary = "Lấy danh sách bài viết đang chờ duyệt trong nhóm (Admin only)",
            description = "Lấy các bài viết có trạng thái PENDING trong nhóm. Chỉ Admin mới có quyền truy cập."
    )
    public ApiResponse<Page<PostResponsePage>> getPendingPostsByGroup(
            @PathVariable String groupId,
            @ParameterObject Pageable pageable
    ) {
        return ApiResponse.success(postService.getPendingPostsByGroup(groupId, pageable));
    }

    @PostMapping("post/{postId}/review")
    @Operation(
            summary = "Duyệt hoặc từ chối bài viết (Admin only)",
            description = "Admin duyệt (approve=true) hoặc từ chối (approve=false) bài viết trong nhóm."
    )
    public ApiResponse<?> reviewPost(
            @PathVariable String postId,
            @RequestParam boolean approve
    ) {
        postService.reviewPost(postId, approve);
        return ApiResponse.success(null);
    }
}
