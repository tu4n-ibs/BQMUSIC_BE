package com.example.demo.repository;

import com.example.demo.entity.PostReactionEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PostReactionRepository extends JpaRepository<PostReactionEntity, String> {
}
