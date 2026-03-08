package com.example.demo.entity;

import com.example.demo.common.BaseEntity;
import com.example.demo.model.enum_object.ActionType;
import com.example.demo.model.enum_object.TargetNotiType;
import jakarta.persistence.Entity;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "notification")
public class NotificationEntity extends BaseEntity {
    @ManyToOne
    private UserEntity userTakeNotification;
    @ManyToOne
    private UserEntity userMakeNotification;
    private ActionType actionType; //    LIKE, COMMENT, FOLLOW, SHARE
    private TargetNotiType targetType; //      POST, ALBUM, COMMENT, GROUP
    private String targetId;
    private Boolean isRead;
}
