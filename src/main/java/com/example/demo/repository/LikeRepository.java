package com.example.demo.repository;

import com.example.demo.entity.LikeEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface LikeRepository extends JpaRepository<LikeEntity, String> {

    boolean existsByPost_IdAndUser_Id(String postId, String userId);

    void deleteByPost_IdAndUser_Id(String postId, String userId);

    long countByPost_Id(String postId);
}
