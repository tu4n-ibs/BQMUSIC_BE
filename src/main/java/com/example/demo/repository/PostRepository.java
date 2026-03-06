package com.example.demo.repository;

import com.example.demo.entity.PostEntity;
import com.example.demo.model.enum_object.ApprovalStatus;
import com.example.demo.model.enum_object.ContextType;
import com.example.demo.model.enum_object.TargetType;
import com.example.demo.model.enum_object.Visibility;
import org.springframework.data.repository.query.Param;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface PostRepository extends JpaRepository<PostEntity,String> {
    long countByUserEntity_Id(String userEntityId);
    long countByUserEntity_IdAndPostTypeAndContextTypeAndApprovalStatus(String userEntityId, com.example.demo.model.enum_object.PostType postType, com.example.demo.model.enum_object.ContextType contextType, com.example.demo.model.enum_object.ApprovalStatus approvalStatus);

    @Query("""
    SELECT p FROM PostEntity p
    WHERE p.userEntity.id = :userId
      AND p.contextType = :contextType
      AND (:postType IS NULL OR p.postType = :postType)
      AND (p.approvalStatus = :approvalStatus OR p.approvalStatus IS NULL)
      AND (
          p.visibility != :privateVisibility
          OR p.userEntity.id = :currentUserId
      )
    ORDER BY p.createdAt DESC
""")
    Page<PostEntity> findPostsByUserId(
            @Param("userId") String userId,
            @Param("currentUserId") String currentUserId,
            @Param("contextType") ContextType contextType,
            @Param("postType") com.example.demo.model.enum_object.PostType postType,
            @Param("approvalStatus") ApprovalStatus approvalStatus,
            @Param("privateVisibility") Visibility privateVisibility,
            Pageable pageable
    );

    @Query("""
    SELECT p FROM PostEntity p
    WHERE p.userEntity.id = :userId
      AND p.contextType = com.example.demo.model.enum_object.ContextType.PROFILE
      AND (:postType IS NULL OR p.postType = :postType)
    ORDER BY p.createdAt DESC
""")
    Page<PostEntity> findPostsByUserIdForOwner(
            @Param("userId") String userId,
            @Param("postType") com.example.demo.model.enum_object.PostType postType,
            Pageable pageable
    );

    Page<PostEntity> findAllByContextTypeIdAndContextTypeAndApprovalStatusOrderByCreatedAtDesc(
            String contextTypeId, ContextType contextType, ApprovalStatus approvalStatus, Pageable pageable);

    @Query("""
       SELECT uf.following.id
       FROM UserFollowEntity uf
       WHERE uf.follower.id = :userId
       """)
    List<String> findFollowingIds(String userId);

    @Query("""
        SELECT DISTINCT p FROM PostEntity p
        JOIN FETCH p.userEntity u
        LEFT JOIN FETCH p.originalPost op
        LEFT JOIN FETCH op.userEntity opu
        WHERE p.userEntity.id IN :followingIds
        AND p.visibility IN :visibilities
        AND p.approvalStatus = :approvalStatus
        ORDER BY p.createdAt DESC
    """)
    List<PostEntity> findPostsByFollowings(
            @Param("followingIds")   List<String>     followingIds,
            @Param("visibilities")   List<Visibility> visibilities,
            @Param("approvalStatus") ApprovalStatus   approvalStatus,
            Pageable pageable
    );

    @Query("""
        SELECT DISTINCT p FROM PostEntity p
        JOIN FETCH p.userEntity u
        LEFT JOIN FETCH p.originalPost op
        LEFT JOIN FETCH op.userEntity opu
        WHERE p.contextType = :contextType
        AND p.contextTypeId IN :groupIds
        AND p.approvalStatus = :approvalStatus
        AND p.visibility = :visibility
        ORDER BY p.createdAt DESC
    """)
    List<PostEntity> findPostsByGroups(
            @Param("contextType")    ContextType    contextType,
            @Param("groupIds")       List<String>   groupIds,
            @Param("approvalStatus") ApprovalStatus approvalStatus,
            @Param("visibility")     Visibility     visibility,
            Pageable pageable
    );

    @Query("""
        SELECT DISTINCT p FROM PostEntity p
        JOIN FETCH p.userEntity u
        LEFT JOIN FETCH p.originalPost op
        LEFT JOIN FETCH op.userEntity opu
        JOIN SongEntity s ON s.id = p.targetId
        WHERE p.targetType = :targetType
        AND s.genre.id IN :genreIds
        AND p.visibility = :visibility
        AND p.approvalStatus = :approvalStatus
        ORDER BY p.createdAt DESC
    """)
    List<PostEntity> findPostsByGenres(
            @Param("targetType") TargetType targetType,
            @Param("genreIds")       List<String>   genreIds,
            @Param("visibility")     Visibility     visibility,
            @Param("approvalStatus") ApprovalStatus approvalStatus,
            Pageable pageable
    );}
