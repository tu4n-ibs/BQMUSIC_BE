package com.example.demo.service.content_service;

import com.example.demo.common.AppException;
import com.example.demo.common.SecurityUtils;
import com.example.demo.entity.AlbumEntity;
import com.example.demo.entity.AlbumSongEntity;
import com.example.demo.entity.SongEntity;
import com.example.demo.entity.UserEntity;
import com.example.demo.model.content_dto.AlbumCreateRequest;
import com.example.demo.model.content_dto.AlbumListResponse;
import com.example.demo.model.content_dto.AlbumResponseDetail;
import com.example.demo.model.content_dto.AlbumSongDto;
import com.example.demo.model.enum_object.Status;
import com.example.demo.repository.AlbumRepository;
import com.example.demo.repository.AlbumSongRepository;
import com.example.demo.repository.SongRepository;
import com.example.demo.repository.UserRepository;
import com.example.demo.service.CloudinaryServiceForImage;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional
public class AlbumService {
    private final AlbumRepository albumRepository;
    private final UserRepository userRepository;
    private final SongRepository songRepository;
    private final AlbumSongRepository albumSongRepository;
    private final CloudinaryServiceForImage cloudinaryServiceForImage;
    public AlbumEntity save(MultipartFile file , AlbumCreateRequest request) {
        String userId = SecurityUtils.getCurrentUserId();
        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new AppException(
                        HttpStatus.NOT_FOUND,
                        "ALBUM_001",
                        "Cannot found this album"
                ));
        String fileName = cloudinaryServiceForImage.uploadFile(file);
        AlbumEntity album = new AlbumEntity();
        album.setImageUrl(fileName);
        album.setName(request.getName());
        album.setDescription(request.getDescription());
        album.setUser(user);

        return albumRepository.save(album);
    }

    public List<AlbumListResponse> findAll() {
        String userId = SecurityUtils.getCurrentUserId();
        return albumRepository.findByUser_Id(userId).stream()
                .map(this::mapToListResponse)
                .toList();
    }

    public List<AlbumListResponse> findAllByUser(String userId) {
        return albumRepository.findByUser_Id(userId).stream()
                .map(this::mapToListResponse)
                .toList();
    }

    private AlbumListResponse mapToListResponse(AlbumEntity entity) {
        return AlbumListResponse.builder()
                .id(entity.getId())
                .name(entity.getName())
                .description(entity.getDescription())
                .imageUrl(entity.getImageUrl())
                .albumImageUrl(entity.getImageUrl()) // common field
                .userId(entity.getUser() != null ? entity.getUser().getId() : null)
                .username(entity.getUser() != null ? entity.getUser().getName() : null)
                .nameUser(entity.getUser() != null ? entity.getUser().getName() : null)
                .songCount(albumSongRepository.countByAlbumEntity_Id(entity.getId()))
                .createdAt(entity.getCreatedAt())
                .build();
    }
    @Transactional
    public void addSong(AlbumSongDto albumSongDto) {

        String userId = SecurityUtils.getCurrentUserId();

        AlbumEntity album = albumRepository.findById(albumSongDto.getAlbumId())
                .orElseThrow(() -> new AppException(
                        HttpStatus.NOT_FOUND,
                        "ALBUM_001",
                        "Cannot found this album"
                ));

        SongEntity song = songRepository.findById(albumSongDto.getSongId())
                .orElseThrow(() -> new AppException(
                        HttpStatus.NOT_FOUND,
                        "SONG_001",
                        "Cannot found this song"
                ));
        if (albumSongRepository.existsBySongEntity(song)){
            throw new  AppException(HttpStatus.CONFLICT,"ALBUM_SONG_001","this song already exists");
        }
        if (!userId.equals(album.getUser().getId())) {
            throw new AppException(
                    HttpStatus.FORBIDDEN,
                    "ALBUM_403",
                    "You are not allowed to modify this album"
            );
        }

        if (!userId.equals(song.getUser().getId())) {
            throw new AppException(
                    HttpStatus.FORBIDDEN,
                    "SONG_403",
                    "You are not allowed to add this song"
            );
        }
        song.setStatus(Status.PUBLISHED);
        songRepository.save(song);
        Optional<Integer> opTrack =
                albumSongRepository.findTopTrackByAlbumEntity_IdOrderByTrackNumberDescDesc(album.getId());

        int trackNumber = opTrack.map(t -> t + 1).orElse(1);

        albumSongRepository.save(new AlbumSongEntity(album, song, trackNumber));
    }

    public void updateImageAlbum(MultipartFile file, String albumId) {

        String userId = SecurityUtils.getCurrentUserId();

        AlbumEntity albumEntity = albumRepository.findById(albumId)
                .orElseThrow(() -> new AppException(
                        HttpStatus.NOT_FOUND,
                        "ALBUM_001",
                        "Cannot found this album"
                ));

        if (!userId.equals(albumEntity.getUser().getId())) {
            throw new AppException(
                    HttpStatus.FORBIDDEN,
                    "ALBUM_403",
                    "You are not allowed to update this album"
            );
        }

        String fileName = cloudinaryServiceForImage.uploadFile(file);

        albumEntity.setImageUrl(fileName);

        albumRepository.save(albumEntity);
    }
    public AlbumResponseDetail getAlbumDetail(String albumId) {
        // 1. Tìm kiếm Album theo ID, ném lỗi nếu không tồn tại
        AlbumEntity albumEntity = albumRepository.findById(albumId)
                .orElseThrow(() -> new AppException(
                        HttpStatus.NOT_FOUND,
                        "ALBUM_001",
                        "Cannot found this album"
                ));

        List<AlbumSongEntity> albumSongs = albumSongRepository.findByAlbumEntity_Id(albumId);

        // 3. Map danh sách các Entity bài hát sang chuẩn Response DTO
        List<AlbumResponseDetail.SongResponseFromAlbum> songResponses = albumSongs.stream()
                .map(albumSong -> {
                    SongEntity song = albumSong.getSongEntity();
                    return AlbumResponseDetail.SongResponseFromAlbum.builder()
                            .songId(song.getId())
                            .songName(song.getName())
                            .songImageUrl(song.getImageUrl())
                            .duration(song.getDuration()) // Giả định có getter getDuration()
                            .build();
                })
                .toList(); // Ghi chú: Sử dụng .collect(Collectors.toList()) nếu bạn đang dùng Java version <= 15

        // 4. Khởi tạo và trả về đối tượng AlbumResponseDetail tổng
        return AlbumResponseDetail.builder()
                .name(albumEntity.getName())
                .description(albumEntity.getDescription())
                .imageUrl(albumEntity.getImageUrl())
                .albumImageUrl(albumEntity.getImageUrl())
                .nameUser(albumEntity.getUser() != null ? albumEntity.getUser().getName() : "Unknown")
                .username(albumEntity.getUser() != null ? albumEntity.getUser().getName() : "Unknown")
                .userId(albumEntity.getUser() != null ? albumEntity.getUser().getId() : null)
                .createdAt(albumEntity.getCreatedAt())
                .songs(songResponses)
                .build();
    }
}
