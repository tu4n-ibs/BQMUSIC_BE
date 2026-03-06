package com.example.demo.model.content_dto;

import com.example.demo.entity.PostEntity;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

@Getter
@Builder
public class ScoredPost {

    private final PostEntity post;
    private double score;

    // Trọng số nguồn
    public static final double WEIGHT_FOLLOWING = 30.0;
    public static final double WEIGHT_GROUP     = 20.0;
    public static final double WEIGHT_GENRE     = 10.0;
    public static final double MAX_RECENCY      = 10.0;
    public static final double RECENCY_DECAY_HOURS = 6.0; // -1 điểm mỗi 6 tiếng

    public void addScore(double delta) {
        this.score += delta;
    }

    /**
     * Tính recency bonus: bài đăng cách đây càng ít giờ, điểm càng cao.
     */
    public void applyRecencyBonus() {
        if (post.getCreatedAt() == null) return;
        long hoursOld = ChronoUnit.HOURS.between(post.getCreatedAt(), LocalDateTime.now());
        double bonus = Math.max(0, MAX_RECENCY - (hoursOld / RECENCY_DECAY_HOURS));
        this.score += bonus;
    }
}
