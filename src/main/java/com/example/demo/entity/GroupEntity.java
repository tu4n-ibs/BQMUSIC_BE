package com.example.demo.entity;

import com.example.demo.common.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.*;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Table(name = "groups")
public class GroupEntity extends BaseEntity {
    private String name;
    @Column(columnDefinition = "TEXT")
    private String description;
    private String imageUrl;
    Boolean isPrivate;
    @Column(name = "require_post_approval")
    private Boolean requirePostApproval;
}
