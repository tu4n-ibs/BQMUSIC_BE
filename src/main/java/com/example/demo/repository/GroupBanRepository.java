package com.example.demo.repository;

import com.example.demo.entity.GroupBanEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface GroupBanRepository extends JpaRepository<GroupBanEntity,String> {

    boolean existsByGroup_IdAndUserBan_Id(String groupId, String userBanId);
}
