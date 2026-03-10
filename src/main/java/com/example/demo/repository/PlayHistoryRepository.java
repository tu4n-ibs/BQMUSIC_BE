package com.example.demo.repository;

import com.example.demo.entity.PlayHistoryEntity;
import org.springframework.data.repository.query.Param;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
@Repository
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
    SELECT s.id, COUNT(ph.id)
    FROM PlayHistoryEntity ph
    JOIN ph.song s
    LEFT JOIN s.genre g
    WHERE (:genreId IS NULL OR g.id = :genreId)
      AND ph.playedAt >= :from
      AND s.status = :status
    GROUP BY s.id
    ORDER BY COUNT(ph.id) DESC
    """)
    List<Object[]> findTopSongs(
            @Param("genreId") String genreId,
            @Param("from")    LocalDateTime from,
            @Param("status")  com.example.demo.model.enum_object.Status status,
            Pageable pageable);


        @Query("""
        SELECT h FROM PlayHistoryEntity h
        JOIN FETCH h.song s
        JOIN FETCH s.user u
        LEFT JOIN FETCH s.genre g
        WHERE h.user.id = :userId
        ORDER BY h.playedAt DESC
    """)
        Slice<PlayHistoryEntity> findByUserIdOrderByPlayedAtDesc(
                @Param("userId") String userId,
                Pageable pageable
        );
}

