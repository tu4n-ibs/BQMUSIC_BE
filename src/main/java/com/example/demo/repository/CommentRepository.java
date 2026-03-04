package com.example.demo.repository;

import com.example.demo.entity.CommentEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;


public interface CommentRepository extends JpaRepository<CommentEntity, String> {

    Page<CommentEntity> findAllByParentComment_Id(String parentCommentId, Pageable pageable);

    Page<CommentEntity> findAllByPost_IdAndParentCommentIsNull(String postId, Pageable pageable);

    Long countByPost_Id(String postId);
}
