package com.example.demo.entity;

import com.example.demo.common.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

@Table(name = "comment")
@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CommentEntity extends BaseEntity {
    @Column(columnDefinition = "TEXT")
    private String content;
    @ManyToOne
    private PostEntity post;
    @ManyToOne
    private UserEntity user;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_comment_id")
    private CommentEntity ParentComment;
    private Integer depth;
}
