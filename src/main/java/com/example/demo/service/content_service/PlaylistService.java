package com.example.demo.service.content_service;

import com.example.demo.common.AppException;
import com.example.demo.common.SecurityUtils;
import com.example.demo.entity.PlayListEntity;
import com.example.demo.entity.PlayListSongEntity;
import com.example.demo.entity.SongEntity;
import com.example.demo.entity.UserEntity;
import com.example.demo.model.content_dto.PlayListSongDto;
import com.example.demo.model.content_dto.PlaylistCreateRequest;
import com.example.demo.repository.PlaylistRepository;
import com.example.demo.repository.PlaylistSongRepository;
import com.example.demo.repository.SongRepository;
import com.example.demo.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PlaylistService {
    private final PlaylistRepository playlistRepository;
    private final PlaylistSongRepository playlistSongRepository;
    private final SongRepository songRepository;
    private final UserRepository userRepository;

    public void save(PlaylistCreateRequest request) {
        String userId = SecurityUtils.getCurrentUserId();
        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new AppException(
                        HttpStatus.NOT_FOUND,
                        "PLAYLIST_001",
                        "Cannot found playlist"
                ));
        PlayListEntity playlist = new PlayListEntity();
        playlist.setName(request.getName());
        playlist.setDescription(request.getDescription());
        playlist.setUser(user);

        playlistRepository.save(playlist);
    }

    public List<PlayListEntity> findAll() {
        return playlistRepository.findAll();
    }

    public void addNewSong(PlayListSongDto request) {

        String userId = SecurityUtils.getCurrentUserId();

        PlayListEntity playlist = playlistRepository.findById(request.getPlayListId())
                .orElseThrow(() -> new AppException(
                        HttpStatus.NOT_FOUND,
                        "PLAYLIST_001",
                        "Cannot found playlist"
                ));

        SongEntity song = songRepository.findById(request.getSongId())
                .orElseThrow(() -> new AppException(
                        HttpStatus.NOT_FOUND,
                        "SONG_001",
                        "Cannot found song"
                ));

        if (!userId.equals(playlist.getUser().getId())) {

            throw new AppException(
                    HttpStatus.FORBIDDEN,
                    "PLAYLIST_403",
                    "You are not allowed to add song to this playlist"
            );
        }

        boolean alreadyExists = playlistSongRepository
                .existsBySongEntityAndPlayListEntity(song, playlist);

        if (alreadyExists) {
            throw new AppException(
                    HttpStatus.BAD_REQUEST,
                    "PLAYLIST_002",
                    "Song already exists in playlist"
            );
        }

        PlayListSongEntity playlistSong = new PlayListSongEntity();
        playlistSong.setSongEntity(song);
        playlistSong.setPlayListEntity(playlist);

        playlistSongRepository.save(playlistSong);
    }

}
