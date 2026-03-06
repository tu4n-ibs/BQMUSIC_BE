package com.example.demo.repository;

import com.example.demo.entity.LikeEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Set;

@Repository
public interface LikeRepository extends JpaRepository<LikeEntity, String> {

    boolean existsByPost_IdAndUser_Id(String postId, String userId);

    void deleteByPost_IdAndUser_Id(String postId, String userId);

    long countByPost_Id(String postId);

    @Query("""
       SELECT l.post.id, COUNT(l)
       FROM LikeEntity l
       WHERE l.post.id IN :postIds
       GROUP BY l.post.id
       """)
    List<Object[]> countLikesByPostIds(List<String> postIds);

    @Query("""
       SELECT l.post.id
       FROM LikeEntity l
       WHERE l.user.id = :userId
       AND l.post.id IN :postIds
       """)
    Set<String> findLikedPostIdsByUserIdAndPostIds(String userId, List<String> postIds);}
