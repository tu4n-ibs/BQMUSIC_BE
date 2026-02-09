package com.example.demo.service;

import com.example.demo.entity.GenreEntity;
import com.example.demo.entity.SongEntity;
import com.example.demo.entity.UserEntity;
import com.example.demo.model.CreateSongRequest;
import com.example.demo.model.SongResponse;
import com.example.demo.repository.GenreRepository;
import com.example.demo.repository.SongRepository;
import com.example.demo.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@Service
@RequiredArgsConstructor
public class SongService {

    private final SongRepository songRepository;
    private final CloudinaryService cloudinaryService;

    private final UserRepository userRepository;
    private final GenreRepository genreRepository;
    public Page<SongResponse> getAllSongs(Pageable pageable) {
        // 1. Lấy Page<SongEntity> từ Repository
        Page<SongEntity> songPage = songRepository.findAll(pageable);

        // 2. Chuyển đổi (Map) từ Entity sang DTO
        return songPage.map(song -> SongResponse.builder()
                .id(song.getId())
                .name(song.getName())
                .musicUrl(song.getMusicUrl())
                .imageUrl(song.getImageUrl())
                .duration(song.getDuration())
                .playCount(song.getPlayCount())
                // Xử lý null an toàn cho User và Genre
                .artistName(song.getUser() != null ? song.getUser().getName() : "Unknown Artist")
                .genreName(song.getGenre() != null ? song.getGenre().getName() : "Unknown Genre")
                .build());
    }
    public void save(CreateSongRequest request, MultipartFile musicFile) {
        try {

            String musicUrl = cloudinaryService.uploadFile(musicFile);

            UserEntity user = userRepository.findById(request.getUserId())
                    .orElseThrow(() -> new RuntimeException("User not found with id: " + request.getUserId()));

            GenreEntity genre = genreRepository.findById(request.getGenreId())
                    .orElseThrow(() -> new RuntimeException("Genre not found with id: " + request.getGenreId()));

            SongEntity song = new SongEntity();
            song.setName(request.getName());
            song.setMusicUrl(musicUrl);
            song.setUser(user);
            song.setGenre(genre);

            song.setPlayCount(0);
            song.setDuration(0);
            song.setImageUrl("https://placehold.co/400");

            songRepository.save(song);

        } catch (IOException e) {
            throw new RuntimeException("Lỗi khi upload file nhạc: " + e.getMessage());
        }
    }
}
