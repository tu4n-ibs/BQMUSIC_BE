package com.example.demo.entity;

import com.example.demo.common.BaseEntity;
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
@Table(name = "album_song")
public class AlbumSongEntity extends BaseEntity {
    @ManyToOne
    private AlbumEntity albumEntity;
    @ManyToOne
    private SongEntity songEntity;
    private Integer trackNumber;
}
