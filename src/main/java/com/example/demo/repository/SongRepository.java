package com.example.demo.repository;

import com.example.demo.entity.SongEntity;
import org.springframework.data.repository.query.Param;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;


public interface SongRepository extends JpaRepository<SongEntity, String> , JpaSpecificationExecutor<SongEntity> {


    Slice<SongEntity> findByNameContainingIgnoreCase(String name, Pageable pageable);

    @Modifying
    @Query("UPDATE SongEntity s SET s.playCount = s.playCount + 1 WHERE s.id = :songId")
    int incrementPlayCount(@Param("songId") String songId);}
