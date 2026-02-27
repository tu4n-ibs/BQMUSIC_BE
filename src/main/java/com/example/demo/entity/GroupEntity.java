package com.example.demo.entity;

import com.example.demo.common.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
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
@Table(name = "groups")
public class GroupEntity extends BaseEntity {
    private String name;
    @Column(columnDefinition = "TEXT")
    private String description;
    private String imageUrl;
    Boolean isPrivate;
}
