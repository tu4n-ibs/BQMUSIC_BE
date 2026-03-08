package com.example.demo.controller;

import com.example.demo.common.SecurityUtils;
import com.example.demo.model.ApiResponse;
import com.example.demo.model.content_dto.SongInHistoryDto;
import com.example.demo.service.content_service.PlayHistoryService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/play-history")
public class PlayHistoryController {
    private final PlayHistoryService playHistoryService;

    @GetMapping
    @Operation(
            summary = "Lấy lịch sử nghe nhạc",
            description = """
            Trả về danh sách bài hát đã nghe của người dùng hiện tại, sắp xếp theo thời gian mới nhất.
            
            **Quy tắc:**
            - Kết quả được phân trang theo kiểu `Slice` (scroll vô tận).
            - Mặc định `page=0`, `size=20`.
            """
    )
    public ApiResponse<Slice<SongInHistoryDto>> getHistory(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        String currentUserId = SecurityUtils.getCurrentUserId();
        Pageable pageable = PageRequest.of(page, size);
        return ApiResponse.success(
                playHistoryService.getSongInHistoryByUserId(currentUserId, pageable)
        );
    }
}
