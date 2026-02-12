package com.example.demo.controller;

import com.example.demo.entity.PlayListEntity;
import com.example.demo.model.ApiResponse;
import com.example.demo.model.content_dto.PlayListSongDto;
import com.example.demo.model.content_dto.PlaylistCreateRequest;
import com.example.demo.service.content_service.PlaylistService;
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

    @GetMapping
    public ApiResponse<List<PlayListEntity>> getPlaylists() {
        return ApiResponse.success(playlistService.findAll());
    }

    @PostMapping("add-new-song")
    public ApiResponse<?> addNewSong(@RequestBody PlayListSongDto playListSongDto) {
        playlistService.addNewSong(playListSongDto);
        return ApiResponse.success(null);
    }
}