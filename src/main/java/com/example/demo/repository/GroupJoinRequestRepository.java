package com.example.demo.repository;

import com.example.demo.entity.GroupJoinRequestEntity;
import com.example.demo.model.enum_object.GroupJoinStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface GroupJoinRequestRepository extends JpaRepository<GroupJoinRequestEntity,String > {
    boolean existsByGroup_IdAndUser_IdAndGroupJoinStatus(String groupId, String userId, GroupJoinStatus groupJoinStatus);
}
