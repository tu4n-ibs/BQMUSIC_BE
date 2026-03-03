package com.example.demo.service.content_service;

import com.example.demo.common.AppException;
import com.example.demo.common.SecurityUtils;
import com.example.demo.entity.PlayListEntity;
import com.example.demo.entity.PlayListSongEntity;
import com.example.demo.entity.SongEntity;
import com.example.demo.entity.UserEntity;
import com.example.demo.model.content_dto.PlayListSongDto;
import com.example.demo.model.content_dto.PlaylistCreateRequest;
import com.example.demo.model.content_dto.SongPlayListResponse;
import com.example.demo.model.content_dto.UserPlaylistResponse;
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
    public List<SongPlayListResponse> songPlayListResponse(String playListId) {
        // 1. Tìm tất cả các bản ghi trong bảng trung gian playlist_song theo playListId
        List<PlayListSongEntity> playlistSongs = playlistSongRepository.findByPlayListEntity_Id(playListId);

        // 2. Lấy số lượng bài hát trong playlist để điền vào field songCount (nếu cần)
        long songCount = playlistSongs.size();

        // 3. Map dữ liệu sang SongPlayListResponse
        return playlistSongs.stream().map(ps -> {
            SongEntity song = ps.getSongEntity();
            PlayListEntity playlist = ps.getPlayListEntity();
            UserEntity artist = song.getUser(); // Giả định UserEntity ở đây là nghệ sĩ sáng tác

            return SongPlayListResponse.builder()
                    .songId(song.getId())
                    .songName(song.getName())
                    .songImage(song.getImageUrl())
                    .songArtistId(artist != null ? artist.getId() : null)
                    .songArtistName(artist != null ? artist.getName() : "Unknown Artist")
                    // Nếu SongEntity không có field Album trực tiếp,
                    // bạn có thể để null hoặc lấy từ GroupEntity nếu Group là Album
                    .songAlbum(song.getGroup() != null ? song.getGroup().getId() : null)
                    .albumName(song.getGroup() != null ? song.getGroup().getName() : "Single")
                    .songCount(songCount)
                    .playlistName(playlist.getName())
                    .playlistId(playlist.getId())
                    .build();
        }).toList();
    }
    public List<UserPlaylistResponse> getAllPlaylistsByUserId(String userId) {
        // 1. Lấy danh sách playlist của User
        List<PlayListEntity> playlists = playlistRepository.findByUser_Id(userId);

        // 2. Map sang DTO UserPlaylistResponse
        return playlists.stream().map(playlist -> {
            // Đếm số bài hát có trong playlist này
            long count = playlistSongRepository.countByPlayListEntity_Id(playlist.getId());

            return UserPlaylistResponse.builder()
                    .playlistId(playlist.getId())
                    .playlistName(playlist.getName())
                    .songCount(count)
                    .createdAt(playlist.getCreatedAt()) // Giả định BaseEntity có field này
                    .build();
        }).toList();
    }
}
