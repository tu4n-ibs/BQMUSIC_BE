package com.example.demo.repository;

import com.example.demo.entity.AlbumEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AlbumRepository extends JpaRepository<AlbumEntity,String> {
}
