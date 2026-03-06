package com.example.demo.repository;

import com.example.demo.entity.AlbumEntity;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AlbumRepository extends JpaRepository<AlbumEntity,String> {
    java.util.List<AlbumEntity> findByUser_Id(String userId);

    Slice<AlbumEntity> findByNameContainingIgnoreCase(String name, Pageable pageable);
}
