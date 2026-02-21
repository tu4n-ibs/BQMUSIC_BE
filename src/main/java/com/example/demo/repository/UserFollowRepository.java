package com.example.demo.repository;

import com.example.demo.entity.UserEntity;
import com.example.demo.entity.UserFollowEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserFollowRepository extends JpaRepository<UserFollowEntity, String> {
    boolean existsByFollowerAndFollowing(UserEntity follower, UserEntity following);

    Optional<UserFollowEntity> findByFollowerAndFollowing(UserEntity follower, UserEntity following);
}
