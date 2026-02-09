package com.example.demo.entity;

import com.example.demo.common.BaseEntity;
import jakarta.persistence.Column;
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
    private Integer duration;
}
