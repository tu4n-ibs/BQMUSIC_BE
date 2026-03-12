package com.example.demo.service.content_service;

import com.example.demo.common.AppException;
import com.example.demo.common.SecurityUtils;
import com.example.demo.entity.GenreEntity;
import com.example.demo.entity.PlayHistoryEntity;
import com.example.demo.entity.SongEntity;
import com.example.demo.entity.UserEntity;
import com.example.demo.model.content_dto.CreateSongRequest;
import com.example.demo.model.content_dto.SongResponse;
import com.example.demo.model.content_dto.TopSongResponse;
import com.example.demo.model.enum_object.ChartPeriod;
import com.example.demo.model.enum_object.Status;
import com.example.demo.repository.GenreRepository;
import com.example.demo.repository.PlayHistoryRepository;
import com.example.demo.repository.SongRepository;
import com.example.demo.repository.UserRepository;
import com.example.demo.service.CloudinaryServiceForImage;
import com.example.demo.service.CloudinaryServiceForMusic;
import com.example.demo.specification.SongSpecification;
import com.mpatric.mp3agic.Mp3File;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class SongService {

    private final SongRepository songRepository;
    private final CloudinaryServiceForMusic cloudinaryServiceForMusic;
    private final CloudinaryServiceForImage cloudinaryServiceForImage;
    private final UserRepository userRepository;
    private final GenreRepository genreRepository;
    private final PlayHistoryRepository playHistoryRepository;

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
    @org.springframework.transaction.annotation.Transactional(readOnly = true)
    public SongResponse getSongById(String songId) {
        SongEntity song = songRepository.findById(songId)
                .orElseThrow(() -> new AppException(HttpStatus.NOT_FOUND, "SONG_001", "Cannot find song"));
        SongResponse response = convertToResponse(song);
        response.setMusicUrl(song.getMusicUrl());
        return response;
    }
    @Transactional
    public void recordPlay(String songId, String userId,Integer duration) {
        int updated = songRepository.incrementPlayCount(songId);
        if (updated == 0) {
            throw new AppException(HttpStatus.NOT_FOUND, "SONG_001", "Cannot find song");
        }

        // 2. Lưu history cho chart theo thời gian
        SongEntity  song    = songRepository.getReferenceById(songId);
        UserEntity  user    = userRepository.getReferenceById(userId);

        PlayHistoryEntity history = new PlayHistoryEntity();
        history.setSong(song);
        history.setUser(user);
        history.setDurationPlayed(duration);
        history.setPlayedAt(LocalDateTime.now());
        playHistoryRepository.save(history);
    }
    @org.springframework.transaction.annotation.Transactional(readOnly = true)
    public Slice<TopSongResponse> getTopSongs(ChartPeriod period, String genreId, Pageable pageable) {
        LocalDateTime from = period.fromDate();

        // Fetch thêm 1 phần tử để detect hasNext
        Pageable fetchPage = PageRequest.of(
                pageable.getPageNumber(),
                pageable.getPageSize() + 1
        );

        // Đảm bảo genreId là null nếu là chuỗi rỗng để query IS NULL hoạt động đúng
        String effectiveGenreId = (genreId != null && genreId.trim().isEmpty()) ? null : genreId;

        List<Object[]> rows = playHistoryRepository.findTopSongs(effectiveGenreId, from, Status.PUBLISHED, fetchPage);

        boolean hasNext = rows.size() > pageable.getPageSize();
        if (hasNext) rows = rows.subList(0, pageable.getPageSize());

        // 1. Thu thập tất cả songId từ kết quả query
        List<String> songIds = rows.stream()
                .map(row -> (String) row[0])
                .toList();

        // 2. Fetch tất cả SongEntity trong 1 lần query để tránh N+1 và Lazy loading
        // Sử dụng Map để look up nhanh
        java.util.Map<String, SongEntity> songMap = songRepository.findAllById(songIds).stream()
                .collect(java.util.stream.Collectors.toMap(SongEntity::getId, s -> s));

        // Tính rank tuyệt đối (không reset về 1 ở mỗi page)
        int baseRank = pageable.getPageNumber() * pageable.getPageSize();

        List<TopSongResponse> result = new ArrayList<>();
        for (int i = 0; i < rows.size(); i++) {
            String     songId = (String) rowIdAndCount(rows.get(i))[0];
            long       cnt    = ((Number) rowIdAndCount(rows.get(i))[1]).longValue();
            SongEntity song   = songMap.get(songId);

            if (song == null) continue;

            result.add(TopSongResponse.builder()
                    .rank(baseRank + i + 1)
                    .songId(song.getId())
                    .songName(song.getName())
                    .imageUrl(song.getImageUrl())
                    .musicUrl(song.getMusicUrl())
                    .artistId  (song.getUser()  != null ? song.getUser().getId()    : null)
                    .artistName(song.getUser()  != null ? song.getUser().getName()  : null)
                    .genreId   (song.getGenre() != null ? song.getGenre().getId()   : null)
                    .genreName (song.getGenre() != null ? song.getGenre().getName() : null)
                    .duration(song.getDuration())
                    .playCount(cnt)
                    .build());
        }

        return new SliceImpl<>(result, pageable, hasNext);
    }

    private Object[] rowIdAndCount(Object[] row) { return row; }
}
