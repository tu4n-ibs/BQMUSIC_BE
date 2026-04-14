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
    List<UserFollowEntity> findUserFollowEntitiesByFollower_Id(String followerId);

    List<UserFollowEntity> findUserFollowEntitiesByFollowing_Id(String followingId);

    // Mới: Tìm kiếm Following theo tên
    @org.springframework.data.jpa.repository.Query("SELECT uf FROM UserFollowEntity uf WHERE uf.follower.id = :followerId AND LOWER(uf.following.name) LIKE LOWER(CONCAT('%', :query, '%'))")
    org.springframework.data.domain.Page<UserFollowEntity> findFollowingByUserName(
            @org.springframework.data.repository.query.Param("followerId") String followerId,
            @org.springframework.data.repository.query.Param("query") String query,
            org.springframework.data.domain.Pageable pageable);

    // Mới: Tìm kiếm Followers theo tên
    @org.springframework.data.jpa.repository.Query("SELECT uf FROM UserFollowEntity uf WHERE uf.following.id = :followingId AND LOWER(uf.follower.name) LIKE LOWER(CONCAT('%', :query, '%'))")
    org.springframework.data.domain.Page<UserFollowEntity> findFollowersByUserName(
            @org.springframework.data.repository.query.Param("followingId") String followingId,
            @org.springframework.data.repository.query.Param("query") String query,
            org.springframework.data.domain.Pageable pageable);
}
