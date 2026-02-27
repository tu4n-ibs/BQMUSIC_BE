package com.example.demo.repository;

import com.example.demo.entity.SongEntity;
import com.example.demo.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;

public interface SongRepository extends JpaRepository<SongEntity, String> , JpaSpecificationExecutor<SongEntity> {

}
