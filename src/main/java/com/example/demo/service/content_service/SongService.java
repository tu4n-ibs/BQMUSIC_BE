package com.example.demo.service.content_service;

import com.example.demo.common.AppException;
import com.example.demo.common.SecurityUtils;
import com.example.demo.entity.GenreEntity;
import com.example.demo.entity.SongEntity;
import com.example.demo.entity.UserEntity;
import com.example.demo.model.content_dto.CreateSongRequest;
import com.example.demo.model.content_dto.SongResponse;
import com.example.demo.model.enum_object.Status;
import com.example.demo.repository.GenreRepository;
import com.example.demo.repository.SongRepository;
import com.example.demo.repository.UserRepository;
import com.example.demo.service.CloudinaryServiceForImage;
import com.example.demo.service.CloudinaryServiceForMusic;
import com.example.demo.specification.SongSpecification;
import com.mpatric.mp3agic.Mp3File;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;

@Slf4j
@Service
@RequiredArgsConstructor
public class SongService {

    private final SongRepository songRepository;
    private final CloudinaryServiceForMusic cloudinaryServiceForMusic;
    private final CloudinaryServiceForImage cloudinaryServiceForImage;
    private final UserRepository userRepository;
    private final GenreRepository genreRepository;

    public void updateImage(MultipartFile file, String SongId) {
        SongEntity songEntity = songRepository.findById(SongId)
                .orElseThrow(()->new AppException(HttpStatus.NOT_FOUND,"SONG_001","cannot found song"));
        if (!SecurityUtils.getCurrentUserId().equals(songEntity.getUser().getId())) {
            throw new AppException(
                    HttpStatus.FORBIDDEN,
                    "PLAYLIST_403",
                    "You are not allowed to add song to this playlist"
            );
        }
        String fileName = cloudinaryServiceForImage.uploadFile(file);
        songEntity.setImageUrl(fileName);
        songRepository.save(songEntity);
    }
    public SongResponse saveSongForPost(CreateSongRequest request, MultipartFile musicFile) {
            String userID = SecurityUtils.getCurrentUserId();
            String musicUrl = cloudinaryServiceForMusic.uploadFile(musicFile);

            UserEntity user = userRepository.findById(userID)
                    .orElseThrow(() -> new AppException(
                    HttpStatus.CONFLICT,
                    "EMAIL_EXIST_001",
                    "Email already exists"
            ));

            GenreEntity genre = genreRepository.findById(request.getGenreId())
                    .orElseThrow(() ->new AppException(HttpStatus.NOT_FOUND, "GENER_NF_001", "Cannot find this genre"));

            SongEntity song = new SongEntity();
            song.setName(request.getName());
            song.setMusicUrl(musicUrl);
            song.setUser(user);
            song.setGenre(genre);
            song.setStatus(Status.DRAFT);
            int duration = extractDuration(musicFile);
            song.setPlayCount(0);
            song.setDuration(duration);

            SongEntity savedSong = songRepository.save(song);
            return convertToResponse(savedSong);
    }

    public Page<SongResponse> getSongsByUserWithPagination(Pageable pageable, String userId) {
        Specification<SongEntity> spec = SongSpecification.hasUserId(userId);

        Page<SongEntity> songPage = songRepository.findAll(spec, pageable);

        return songPage.map(this::convertToResponse);
    }

    private SongResponse convertToResponse(SongEntity song) {
        return SongResponse.builder()
                .id(song.getId())
                .name(song.getName())
                .artistName(song.getUser() != null ? song.getUser().getName() : null)
                .genreName(song.getGenre() != null ? song.getGenre().getName() : null)
                .imageUrl(song.getImageUrl())
                .duration(song.getDuration())
                .playCount(song.getPlayCount())
                .build();
    }

    private int extractDuration(MultipartFile musicFile) {
        try {
            File tempFile = File.createTempFile("music_", ".mp3");
            musicFile.transferTo(tempFile);

            Mp3File mp3File = new Mp3File(tempFile);
            long durationInSeconds = mp3File.getLengthInSeconds();

            boolean result = tempFile.delete();
            if (!result) {
                log.error("cannot delete temp file");
            }
            return (int) durationInSeconds;

        } catch (Exception e) {
            return 0;
        }
    }

    public SongResponse getSongById(String songId) {
        SongEntity song = songRepository.findById(songId)
                .orElseThrow(() -> new AppException(HttpStatus.NOT_FOUND, "SONG_001", "Cannot find song"));
        SongResponse response = convertToResponse(song);
        response.setMusicUrl(song.getMusicUrl());
        return response;
    }
}
