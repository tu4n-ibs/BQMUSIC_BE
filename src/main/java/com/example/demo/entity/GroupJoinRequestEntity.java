package com.example.demo.entity;

import com.example.demo.common.BaseEntity;
import com.example.demo.model.enum_object.GroupJoinStatus;
import jakarta.persistence.Entity;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.*;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Table(name = "group_join_request")
public class GroupJoinRequestEntity extends BaseEntity {
    @ManyToOne
    private GroupEntity group;
    @ManyToOne
    private UserEntity user;
    private GroupJoinStatus groupJoinStatus;
}
