package com.example.demo.repository;

import com.example.demo.entity.RoleEntity;
import com.example.demo.entity.UserEntity;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.Set;

@Repository
public interface UserRepository extends JpaRepository<UserEntity, String> {

    @Query("SELECT u FROM UserEntity u WHERE u.id <> :currentUserId " +
            "AND u.id NOT IN (SELECT f.following.id FROM UserFollowEntity f WHERE f.follower.id = :currentUserId)")
    List<UserEntity> findSuggestedUsers(String currentUserId, Pageable pageable);

    Optional<UserEntity> findByEmail(String email);

    boolean existsByEmail(String email);

    boolean existsByRoles(Set<RoleEntity> roles);

    Slice<UserEntity> findByNameContainingIgnoreCase(String keyword, Pageable pageable);
}
