package com.example.demo.entity;

import com.example.demo.common.BaseEntity;
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
@Table(name = "group_ban",uniqueConstraints = {
        @UniqueConstraint(columnNames = {"user_ban", "group_ban"})
})
public class GroupBanEntity extends BaseEntity {
    @ManyToOne
    @JoinColumn(name = "user_ban", nullable = false)
    private UserEntity userBan;
    
    @ManyToOne
    @JoinColumn(name = "group_ban", nullable = false)
    private GroupEntity groupBan;

    @ManyToOne
    @JoinColumn(name = "banned_by")
    private UserEntity bannedBy;
}
