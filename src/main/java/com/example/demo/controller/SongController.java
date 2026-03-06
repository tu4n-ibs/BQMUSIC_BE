package com.example.demo.controller;

import com.example.demo.model.ApiResponse;
import com.example.demo.model.content_dto.CreateSongRequest;
import com.example.demo.model.content_dto.SongResponse;
import com.example.demo.service.content_service.SongService;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/v1/song")
@RequiredArgsConstructor
public class SongController {
    private final SongService songService;
    @PostMapping
    public ApiResponse<SongResponse> saveSong( @ModelAttribute CreateSongRequest createSongRequest, @RequestParam(value = "file") MultipartFile musicFile) {
        SongResponse result = songService.saveSongForPost(createSongRequest, musicFile);
        return ApiResponse.success(result,"Success");
    }
    @GetMapping("songs/{userId}")
    public ApiResponse<Page<SongResponse>> getAllSongs(
            @ParameterObject Pageable pageable,@PathVariable String userId
    ) {

        Page<SongResponse> result = songService.getSongsByUserWithPagination(pageable,userId);

        return ApiResponse.success(result, "Lấy danh sách thành công");
    }
    @PutMapping("/update-image")
    public ApiResponse<?> updateSongImage(@RequestParam String songId, @RequestParam(value = "file") MultipartFile file) {
        songService.updateImage(file, songId);
        return ApiResponse.success(null,"Success");
    }

    @GetMapping("/{songId}")
    public ApiResponse<SongResponse> getSongById(@PathVariable String songId) {
        SongResponse result = songService.getSongById(songId);
        return ApiResponse.success(result, "Lấy thông tin bài hát thành công");
    }
}
