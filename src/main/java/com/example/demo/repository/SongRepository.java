package com.example.demo.repository;

import com.example.demo.entity.SongEntity;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;


public interface SongRepository extends JpaRepository<SongEntity, String> , JpaSpecificationExecutor<SongEntity> {


    Slice<SongEntity> findByNameContainingIgnoreCase(String name, Pageable pageable);
}
