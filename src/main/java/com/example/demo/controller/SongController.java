package com.example.demo.controller;

import com.example.demo.model.ApiResponse;
import com.example.demo.model.content_dto.CreateSongRequest;
import com.example.demo.model.content_dto.SongResponse;
import com.example.demo.service.content_service.SongService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/v1/song")
@RequiredArgsConstructor
public class SongController {
    private final SongService songService;
    @PostMapping
    public ApiResponse<?> saveSong( @ModelAttribute CreateSongRequest createSongRequest, @RequestParam(value = "file") MultipartFile musicFile) {
        songService.save(createSongRequest, musicFile);
        return ApiResponse.success(null,"Success");
    }
    @GetMapping
    public ApiResponse<Page<SongResponse>> getAllSongs(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("id").descending());

        Page<SongResponse> result = songService.getAllSongs(pageable);

        return ApiResponse.success(result, "Lấy danh sách thành công");
    }
    @PutMapping("/update-image")
    public ApiResponse<?> updateSongImage(@RequestParam String songId, @RequestParam(value = "file") MultipartFile musicFile) {
        songService.updateImage(musicFile, songId);
        return ApiResponse.success(null,"Success");
    }
}
