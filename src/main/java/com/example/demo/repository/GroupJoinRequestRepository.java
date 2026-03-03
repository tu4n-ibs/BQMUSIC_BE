package com.example.demo.repository;

import com.example.demo.entity.GroupJoinRequestEntity;
import com.example.demo.model.enum_object.GroupJoinStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;

@Repository
public interface GroupJoinRequestRepository extends JpaRepository<GroupJoinRequestEntity,String > {
    boolean existsByGroup_IdAndUser_IdAndGroupJoinStatus(String groupId, String userId, GroupJoinStatus groupJoinStatus);


    List<GroupJoinRequestEntity> findAllByGroup_IdAndGroupJoinStatus(String groupId, GroupJoinStatus groupJoinStatus);

}
