package com.example.demo.controller;

import com.example.demo.model.ApiResponse;
import com.example.demo.model.content_dto.AlbumCreateRequest;
import com.example.demo.model.content_dto.AlbumListResponse;
import com.example.demo.model.content_dto.AlbumResponseDetail;
import com.example.demo.model.content_dto.AlbumSongDto;
import com.example.demo.service.content_service.AlbumService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/album")
public class AlbumController {
    private final AlbumService albumService;

    @PostMapping
    public ApiResponse<?> newAlbum(@RequestParam MultipartFile file ,@ModelAttribute AlbumCreateRequest albumCreateRequest) {
        albumService.save(file,albumCreateRequest);
        return ApiResponse.success(null);
    }

    @GetMapping
    public ApiResponse<List<AlbumListResponse>> getAlbums() {
        return ApiResponse.success(albumService.findAll());
    }

    @GetMapping("/user/{userId}")
    public ApiResponse<List<AlbumListResponse>> getAlbumsByUser(@PathVariable String userId) {
        return ApiResponse.success(albumService.findAllByUser(userId));
    }

    @PostMapping("/add-new-song")
    public ApiResponse<?> addNewSong(@RequestBody AlbumSongDto albumSongDto) {
        albumService.addSong(albumSongDto);
        return ApiResponse.success(null);
    }

    @PostMapping("update-image")
    public ApiResponse<?> updateImage(@RequestParam MultipartFile file, @RequestParam String albumId) {
        albumService.updateImageAlbum(file, albumId);
        return ApiResponse.success(null);
    }
    @GetMapping("songs/{albumId}")
    public ApiResponse<AlbumResponseDetail> getSong(@PathVariable String albumId) {
        return ApiResponse.success(albumService.getAlbumDetail(albumId));
    }
}
