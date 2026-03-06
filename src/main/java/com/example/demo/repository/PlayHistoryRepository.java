package com.example.demo.repository;

import com.example.demo.entity.PlayHistoryEntity;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface PlayHistoryRepository extends JpaRepository<PlayHistoryEntity,String> {
    @Query("""
       SELECT s.genre.id, COUNT(ph)
       FROM PlayHistoryEntity ph
       JOIN ph.song s
       WHERE ph.user.id = :userId
       GROUP BY s.genre.id
       ORDER BY COUNT(ph) DESC
       """)
    List<Object[]> findTopGenreIdsByUserId(String userId, Pageable pageable);}
