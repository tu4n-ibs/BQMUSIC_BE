package com.example.demo.entity;

import com.example.demo.common.BaseEntity;
import com.example.demo.model.enum_object.GroupRole;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "group_member")
public class GroupMemberEntity extends BaseEntity {
    @ManyToOne
    private GroupEntity groupEntity;
    @ManyToOne
    private UserEntity userEntity;
    @Enumerated(EnumType.STRING)
    private GroupRole groupRole;
}
