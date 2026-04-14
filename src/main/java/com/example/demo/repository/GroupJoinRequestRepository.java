package com.example.demo.repository;

import com.example.demo.entity.GroupJoinRequestEntity;
import com.example.demo.model.enum_object.GroupJoinStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface GroupJoinRequestRepository extends JpaRepository<GroupJoinRequestEntity,String > {
    boolean existsByGroup_IdAndUser_IdAndGroupJoinStatus(String groupId, String userId, GroupJoinStatus groupJoinStatus);

    List<GroupJoinRequestEntity> getGroupJoinRequestEntitiesByGroupJoinStatusAndGroup_Id(GroupJoinStatus groupJoinStatus, String groupId);
    List<GroupJoinRequestEntity> findAllByGroup_IdAndGroupJoinStatus(String groupId, GroupJoinStatus groupJoinStatus);

    @org.springframework.data.jpa.repository.Query("""
        SELECT gr FROM GroupJoinRequestEntity gr
        WHERE gr.group.id = :groupId
        AND gr.groupJoinStatus = :status
        AND (:query IS NULL OR LOWER(gr.user.name) LIKE LOWER(CONCAT('%', :query, '%')))
        ORDER BY gr.createdAt DESC
    """)
    org.springframework.data.domain.Page<GroupJoinRequestEntity> findPendingRequestsByGroupSearch(
            @org.springframework.data.repository.query.Param("groupId") String groupId,
            @org.springframework.data.repository.query.Param("status") GroupJoinStatus status,
            @org.springframework.data.repository.query.Param("query") String query,
            org.springframework.data.domain.Pageable pageable);
}
