package com.example.demo.repository;

import com.example.demo.entity.GroupMemberEntity;
import com.example.demo.model.enum_object.GroupRole;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface GroupMemberRepository extends JpaRepository<GroupMemberEntity,String> {

    boolean existsByGroupEntity_IdAndUserEntity_Id(String groupEntityId, String userEntityId);

    Optional<GroupMemberEntity> findByGroupEntity_IdAndUserEntity_Id(String groupEntityId, String userEntityId);

    long countByGroupEntity_IdAndGroupRole(String groupEntityId, GroupRole groupRole);
}
