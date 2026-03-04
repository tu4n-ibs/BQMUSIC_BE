package com.example.demo.entity;

import com.example.demo.common.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Table(name = "likes",uniqueConstraints = {
        @UniqueConstraint(columnNames = {"post_id", "user_id"})
})
public class LikeEntity extends BaseEntity {
    @ManyToOne
    private PostEntity post;
    @ManyToOne
    private UserEntity user;
}
