package com.example.demo.service.content_service;

import com.example.demo.entity.GenreEntity;
import com.example.demo.entity.PlayHistoryEntity;
import com.example.demo.entity.SongEntity;
import com.example.demo.entity.UserEntity;
import com.example.demo.model.content_dto.SongInHistoryDto;
import com.example.demo.repository.PlayHistoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PlayHistoryService {
    private final PlayHistoryRepository playHistoryRepository;

    public Slice<SongInHistoryDto> getSongInHistoryByUserId(String userId, Pageable pageable) {
        Slice<PlayHistoryEntity> historySlice = playHistoryRepository
                .findByUserIdOrderByPlayedAtDesc(userId, pageable);

        return historySlice.map(this::toSongInHistoryDto);
    }

    private SongInHistoryDto toSongInHistoryDto(PlayHistoryEntity history) {
        SongEntity song = history.getSong();
        UserEntity artist = song.getUser();
        GenreEntity genre = song.getGenre();

        return SongInHistoryDto.builder()
                .id(song.getId())
                .name(song.getName())
                .imageUrl(song.getImageUrl())
                .musicUrl(song.getMusicUrl())
                .duration(song.getDuration())
                .lastPlayedAt(history.getPlayedAt())
                .artist(SongInHistoryDto.ArtistSummaryDto.builder()
                        .id(artist.getId())
                        .name(artist.getName())
                        .imageUrl(artist.getImageUrl())
                        .build())
                .genre(genre == null ? null : SongInHistoryDto.GenreSummaryDto.builder()
                        .id(genre.getId())
                        .name(genre.getName())
                        .build())
                .build();
    }
}