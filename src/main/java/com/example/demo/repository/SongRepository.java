package com.example.demo.repository;

import com.example.demo.entity.SongEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SongRepository extends JpaRepository<SongEntity, String> {
}
