package com.example.demo.repository;

import com.example.demo.entity.PlayListEntity;
import com.example.demo.entity.PlayListSongEntity;
import com.example.demo.entity.SongEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PlaylistSongRepository extends JpaRepository<PlayListSongEntity,String> {
    boolean existsBySongEntityAndPlayListEntity(SongEntity song, PlayListEntity playlist);

    List<PlayListSongEntity> findByPlayListEntity_Id(String playListEntityId);

    long countByPlayListEntity_Id(String playListEntityId);
}
