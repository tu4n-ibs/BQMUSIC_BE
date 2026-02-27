package com.example.demo.entity;

import com.example.demo.common.BaseEntity;
import com.example.demo.model.enum_object.Status;
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
@Table(name = "song")
public class SongEntity extends BaseEntity {
    private String name ;
    @ManyToOne
    private UserEntity user;
    private String imageUrl;
    private String musicUrl;
    @ManyToOne
    private GenreEntity genre;
    private Integer playCount;
    @Enumerated(EnumType.STRING)
    private Status status;
    private Integer duration;
    @ManyToOne
    private GroupEntity group;
}
