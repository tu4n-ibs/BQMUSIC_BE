package com.example.demo.entity;

import com.example.demo.common.BaseEntity;
import com.example.demo.model.enum_object.*;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Table(name = "post")
public class PostEntity extends BaseEntity {
    @ManyToOne
    private UserEntity userEntity;
    @Enumerated(EnumType.STRING)
    private ContextType contextType;

    private String contextTypeId;

    @Enumerated(EnumType.STRING)
    private PostType postType;

    @ManyToOne
    private PostEntity originalPost;

    private String content;

    private String originalContent;

    @Enumerated(EnumType.STRING)
    private Visibility visibility;

    @Enumerated(EnumType.STRING)
    private TargetType targetType;

    private String targetId;

    @Enumerated(EnumType.ORDINAL)
    private ApprovalStatus approvalStatus;
}
