package com.example.demo.entity;

import com.example.demo.common.BaseEntity;
import com.example.demo.model.enum_object.GroupRole;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Table(name = "group_member")
public class GroupMemberEntity extends BaseEntity {
    @ManyToOne
    private GroupEntity groupEntity;
    @ManyToOne
    private UserEntity userEntity;
    @Enumerated(EnumType.STRING)
    private GroupRole groupRole;
}
