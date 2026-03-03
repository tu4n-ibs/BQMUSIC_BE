package com.example.demo.controller;

import com.example.demo.common.SecurityUtils;
import com.example.demo.model.ApiResponse;
import com.example.demo.model.content_dto.CreateGroupRequest;
import com.example.demo.model.content_dto.GroupJoinRequestResponse;
import com.example.demo.service.content_service.GroupService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/groups")
public class GroupController {
 private final GroupService groupService;

    @PostMapping
    @Operation(
            summary = "Tạo một nhóm mới",
            description = """
            Tạo một nhóm mới với thông tin cơ bản.
            
            **Quy tắc:**
            - Người tạo nhóm sẽ tự động trở thành `ADMIN` của nhóm đó.
            - `name` của nhóm là bắt buộc và không được vượt quá 255 ký tự.
            """
    )
    public ApiResponse<?> createGroup(
            @RequestBody @Valid CreateGroupRequest request
           ) {
        String currentUserId = SecurityUtils.getCurrentUserId();
        groupService.createGroup(request, currentUserId);
        return ApiResponse.success(null);
    }

    @PatchMapping("/{groupId}/toggle-post-approval")
    @Operation(
            summary = "Bật/tắt chế độ phê duyệt bài viết",
            description = """
            Thay đổi trạng thái yêu cầu phê duyệt bài viết trước khi hiển thị trong nhóm.
            
            **Quy tắc:**
            - Người gọi API phải có quyền `ADMIN` trong nhóm.
            - Trạng thái `requirePostApproval` sẽ được đảo ngược (true -> false, false -> true).
            """
    )
    public ApiResponse<?> toggleRequirePostApproval(
            @PathVariable String groupId) {
        String currentUserId = SecurityUtils.getCurrentUserId();

        groupService.toggleRequirePostApproval(groupId, currentUserId);
        return ApiResponse.success(null);
    }

    // ==================== BẬT/TẮT PRIVATE GROUP ====================
    @PatchMapping("/{groupId}/toggle-private")
    @Operation(
            summary = "Bật/tắt chế độ nhóm kín (Private)",
            description = """
            Thay đổi quyền riêng tư của nhóm (Công khai hoặc Kín).
            
            **Quy tắc:**
            - Người gọi API phải có quyền `ADMIN` trong nhóm.
            - Nếu nhóm chuyển sang Kín (Private), người dùng mới phải gửi yêu cầu tham gia và chờ duyệt.
            """
    )
    public ApiResponse<?> togglePrivateGroup(
            @PathVariable String groupId
            ) {
        String currentUserId = SecurityUtils.getCurrentUserId();
        groupService.togglePrivateGroup(groupId, currentUserId);
        return ApiResponse.success(null);
    }

    @PostMapping("/{groupId}/join-requests")
    @Operation(
            summary = "Gửi yêu cầu tham gia nhóm",
            description = """
            Người dùng gửi yêu cầu tham gia vào một nhóm cụ thể.
            
            **Quy tắc:**
            - Nếu nhóm **không Private (Công khai)**: Người dùng được tham gia ngay lập tức và trở thành `MEMBER`.
            - Nếu nhóm **Private**: Yêu cầu sẽ được tạo với trạng thái `PENDING` và cần Admin duyệt.
            - Báo lỗi nếu người dùng đã là thành viên, đã bị Ban khỏi nhóm, hoặc đã có yêu cầu PENDING trước đó.
            """
    )
    public ApiResponse<?> sendJoinRequest(
            @PathVariable String groupId) {
        String currentUserId = SecurityUtils.getCurrentUserId();
        groupService.sendJoinRequest(groupId, currentUserId);
        return ApiResponse.success(null);
    }

    @PatchMapping("/{groupId}/join-requests/{requestId}/review")
    @Operation(
            summary = "Phê duyệt hoặc từ chối yêu cầu tham gia",
            description = """
            Admin của nhóm xử lý yêu cầu tham gia nhóm đang ở trạng thái PENDING.
            
            **Quy tắc:**
            - Người gọi API phải có quyền `ADMIN` trong nhóm.
            - Truyền `approve=true` để duyệt (người dùng thành `MEMBER`).
            - Truyền `approve=false` để từ chối yêu cầu.
            """
    )
    public ApiResponse<?> reviewJoinRequest(
            @PathVariable String groupId,
            @PathVariable String requestId,
            @RequestParam boolean approve,
            @RequestAttribute("userId") String currentUserId) {
        groupService.reviewJoinRequest(groupId, requestId, approve, currentUserId);
        return ApiResponse.success(null);
    }

    @Operation(
            summary = "Lấy danh sách yêu cầu tham gia đang chờ duyệt",
            description = """
    Lấy tất cả các yêu cầu tham gia nhóm đang ở trạng thái `PENDING`.

    **Quy tắc:**
    - Người gọi phải là **thành viên** của nhóm
    - Người gọi phải có vai trò **ADMIN**
    - Chỉ trả về các yêu cầu có trạng thái `PENDING`
    - Nếu nhóm không tồn tại → trả về lỗi `404`
    - Nếu người dùng không phải ADMIN → trả về lỗi `403`
    - Cái groupJoinRequestId dùng để từ chối hoặc chấp nhận làm thành viên
    """
    )
    @GetMapping("/{groupId}/join-requests/pending")
    public ResponseEntity<List<GroupJoinRequestResponse>> getPendingJoinRequests(
            @PathVariable String groupId
    ) {
        String currentUserId = SecurityUtils.getCurrentUserId();
        List<GroupJoinRequestResponse> responses =
                groupService.getPendingRequests(groupId, currentUserId);

        return ResponseEntity.ok(responses);
    }
}
