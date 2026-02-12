package com.example.demo.entity;

import com.example.demo.common.BaseEntity;
import com.example.demo.model.enum_object.ContextType;
import com.example.demo.model.enum_object.PostType;
import com.example.demo.model.enum_object.TargetType;
import com.example.demo.model.enum_object.Visibility;
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

    @Enumerated(EnumType.STRING)
    private Visibility visibility;

    @Enumerated(EnumType.STRING)
    private TargetType targetType;

    private String targetId;
}
