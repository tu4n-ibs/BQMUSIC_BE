package com.example.demo.repository;

import com.example.demo.entity.PostEntity;
import com.example.demo.entity.UserEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PostRepository extends JpaRepository<PostEntity,String> {
    long countByUserEntity_Id(String userEntityId);
}
