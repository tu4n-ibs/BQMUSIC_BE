package com.example.demo.repository;


import com.example.demo.entity.GenreEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface GenreRepository  extends JpaRepository<GenreEntity, String> {
    GenreEntity findByName(String name);
}
