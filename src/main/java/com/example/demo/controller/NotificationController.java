package com.example.demo.controller;

import com.example.demo.common.SecurityUtils;
import com.example.demo.model.ApiResponse;
import com.example.demo.model.NotificationResponse;
import com.example.demo.service.NotificationService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.web.bind.annotation.*;

    @RestController
    @RequiredArgsConstructor
    @RequestMapping("/api/v1/notifications")
    public class NotificationController {
        private final NotificationService notificationService;

        @GetMapping
        @Operation(summary = "Lấy danh sách thông báo",
                description = "Trả về thông báo của người dùng hiện tại, mới nhất trước.")
        public ApiResponse<Slice<NotificationResponse>> getNotifications(
                @RequestParam(defaultValue = "0") int page,
                @RequestParam(defaultValue = "20") int size
        ) {
            String userId = SecurityUtils.getCurrentUserId();
            Pageable pageable = PageRequest.of(page, size);
            return ApiResponse.success(notificationService.getNotifications(userId, pageable));
        }

        @PatchMapping("/{notificationId}/read")
        @Operation(summary = "Đánh dấu đã đọc một thông báo")
        public ApiResponse<?> markAsRead(@PathVariable String notificationId) {
            String userId = SecurityUtils.getCurrentUserId();
            notificationService.markAsRead(notificationId, userId);
            return ApiResponse.success(null);
        }

        @PatchMapping("/read-all")
        @Operation(summary = "Đánh dấu đã đọc tất cả thông báo")
        public ApiResponse<?> markAllAsRead() {
            String userId = SecurityUtils.getCurrentUserId();
            notificationService.markAllAsRead(userId);
            return ApiResponse.success(null);
        }
    }
