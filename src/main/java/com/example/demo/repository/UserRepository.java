package com.example.demo.repository;

import com.example.demo.entity.UserEntity;
import org.apache.catalina.User;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<UserEntity, String> {

    @Query("SELECT u FROM UserEntity u WHERE u.id <> :currentUserId " +
            "AND u.id NOT IN (SELECT f.following.id FROM UserFollow f WHERE f.follower.id = :currentUserId)")
    List<UserEntity> findSuggestedUsers(String currentUserId, Pageable pageable);

    Optional<UserEntity> findByEmail(String email);

    // Nếu bạn cần kiểm tra sự tồn tại nhanh
    boolean existsByEmail(String email);
}
