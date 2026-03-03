package com.example.demo.controller;

import com.example.demo.entity.PlayListEntity;
import com.example.demo.model.ApiResponse;
import com.example.demo.model.content_dto.PlayListSongDto;
import com.example.demo.model.content_dto.PlaylistCreateRequest;
import com.example.demo.model.content_dto.SongPlayListResponse;
import com.example.demo.model.content_dto.UserPlaylistResponse;
import com.example.demo.service.content_service.PlaylistService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/playlist")
public class PlaylistController {

    private final PlaylistService playlistService;

    @PostMapping
    public ApiResponse<?> newPlaylist(@RequestBody PlaylistCreateRequest playlistCreateRequest) {
        playlistService.save(playlistCreateRequest);
        return ApiResponse.success(null);
    }

    @PostMapping("add-new-song")
    public ApiResponse<?> addNewSong(@RequestBody PlayListSongDto playListSongDto) {
        playlistService.addNewSong(playListSongDto);
        return ApiResponse.success(null);
    }
    @Operation(
            summary = "Lấy danh sách bài hát trong một Playlist",
            description = """
Lấy toàn bộ thông tin bài hát thuộc về một Playlist cụ thể.

**Thông tin trả về bao gồm:**
- Chi tiết bài hát (ID, Tên, Ảnh)
- Thông tin nghệ sĩ (User sáng tạo bài hát)
- Thông tin Album (Nếu bài hát thuộc về một Group/Album)
- Tổng số lượng bài hát hiện có trong Playlist này.
"""
    )
    @GetMapping("/{playListId}/songs")
    public ApiResponse<List<SongPlayListResponse>> getSongsFromPlaylist(
            @PathVariable String playListId
    ) {
        // Thực hiện gọi service để lấy dữ liệu
        List<SongPlayListResponse> responses = playlistService.songPlayListResponse(playListId);

        return ApiResponse.success(responses);
    }
    @Operation(
            summary = "Lấy danh sách playlist của một người dùng",
            description = "Trả về danh sách các playlist bao gồm ID, tên và tổng số lượng bài hát bên trong."
    )
    @GetMapping("/user/{userId}")
    public ApiResponse<List<UserPlaylistResponse>> getPlaylistsByUserId(@PathVariable String userId) {
        List<UserPlaylistResponse> responses = playlistService.getAllPlaylistsByUserId(userId);
        return ApiResponse.success(responses);
    }
}