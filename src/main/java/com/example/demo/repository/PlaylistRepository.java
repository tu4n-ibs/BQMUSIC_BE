package com.example.demo.repository;

import com.example.demo.entity.PlayListEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PlaylistRepository extends JpaRepository<PlayListEntity,String> {
    List<PlayListEntity> findByUser_Id(String userId);
}
