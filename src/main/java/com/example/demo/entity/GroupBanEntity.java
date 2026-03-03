package com.example.demo.entity;

import com.example.demo.common.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "group_ban",uniqueConstraints = {
        @UniqueConstraint(columnNames = {"user_ban", "group_ban"})
})
@Builder
public class GroupBanEntity extends BaseEntity {
    @ManyToOne
    @JoinColumn(name = "user_ban", nullable = false)
    private UserEntity userBan;
    
    @ManyToOne
    @JoinColumn(nullable = false)
    private GroupEntity group;

    @ManyToOne
    @JoinColumn(name = "banned_by")
    private UserEntity bannedBy;
}
