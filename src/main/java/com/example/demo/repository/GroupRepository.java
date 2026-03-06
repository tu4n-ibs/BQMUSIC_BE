package com.example.demo.repository;

import com.example.demo.entity.GroupEntity;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface GroupRepository extends JpaRepository<GroupEntity, String> {
    Slice<GroupEntity> findByNameContainingIgnoreCase(String name, Pageable pageable);
}
