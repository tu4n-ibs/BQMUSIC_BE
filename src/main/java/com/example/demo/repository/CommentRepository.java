package com.example.demo.repository;

import com.example.demo.entity.CommentEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;


public interface CommentRepository extends JpaRepository<CommentEntity, String> {

    Page<CommentEntity> findAllByParent_Id(String parentId, Pageable pageable);

    Page<CommentEntity> findAllByPost_IdAndParentIsNull(String postId, Pageable pageable);

    Long countByPost_Id(String postId);

    Long countByParent_Id(String parentId);
}
