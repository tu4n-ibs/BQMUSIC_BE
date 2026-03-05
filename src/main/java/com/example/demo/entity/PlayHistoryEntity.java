package com.example.demo.entity;

import com.example.demo.common.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(
        name = "play_history",
        indexes = {
                @Index(name = "idx_play_history_user", columnList = "user_id"),
                @Index(name = "idx_play_history_song", columnList = "song_id"),
                @Index(name = "idx_play_history_played_at", columnList = "played_at")
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PlayHistoryEntity extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private UserEntity user;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "song_id", nullable = false)
    private SongEntity song;

    @Column(name = "duration_played")
    private Integer durationPlayed;
}
