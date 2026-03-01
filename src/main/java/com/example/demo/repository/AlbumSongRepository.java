package com.example.demo.repository;

import com.example.demo.entity.AlbumSongEntity;
import com.example.demo.entity.SongEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface AlbumSongRepository extends JpaRepository<AlbumSongEntity,String> {
    @Query("select max(a.trackNumber) from  AlbumSongEntity a where a.albumEntity.id = :albumId")
    Optional<Integer> findTopTrackByAlbumEntity_IdOrderByTrackNumberDescDesc(@Param("albumId") String albumId);
    boolean existsBySongEntity(SongEntity songEntity);

    List<AlbumSongEntity> findByAlbumEntity_Id(String albumEntityId);
}
