package com.example.demo.repository;

import com.example.demo.entity.PlayHistoryEntity;
import io.lettuce.core.dynamic.annotation.Param;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDateTime;
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
    List<Object[]> findTopGenreIdsByUserId(String userId, Pageable pageable);

    @Query("""
    SELECT s, COUNT(ph.id) AS playCount
    FROM PlayHistoryEntity ph
    JOIN ph.song s
    WHERE (:genreId IS NULL OR s.genre.id = :genreId)
      AND (:from IS NULL OR ph.playedAt >= :from)
      AND s.status = 'ACTIVE'
    GROUP BY s
    ORDER BY playCount DESC
    """)
    List<Object[]> findTopSongs(
            @Param("genreId") String genreId,
            @Param("from")    LocalDateTime from,
            Pageable pageable);
}

