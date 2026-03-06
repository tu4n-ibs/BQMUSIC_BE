package com.example.demo.repository;

import com.example.demo.entity.UserEntity;
import com.example.demo.entity.UserFollowEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UserFollowRepository extends JpaRepository<UserFollowEntity, String> {
    boolean existsByFollowerAndFollowing(UserEntity follower, UserEntity following);

    Optional<UserFollowEntity> findByFollowerAndFollowing(UserEntity follower, UserEntity following);

    long countByFollower_Id(String followerId);

    long countByFollowing_Id(String followingId);

    boolean existsByFollower_IdAndFollowing_Id(String followerId, String followingId);

    List<UserFollowEntity> findUserFollowByFollower_Id(String followerId);

}
