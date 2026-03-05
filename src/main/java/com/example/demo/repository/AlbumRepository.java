package com.example.demo.repository;

import com.example.demo.entity.AlbumEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;

public interface AlbumRepository extends JpaRepository<AlbumEntity,String> {
    java.util.List<AlbumEntity> findByUser_Id(String userId);
}
