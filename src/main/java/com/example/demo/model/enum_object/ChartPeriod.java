package com.example.demo.model.enum_object;

import java.time.LocalDateTime;

public enum ChartPeriod {
    WEEK_7,
    DAY_30,
    ALL_TIME;

    public LocalDateTime fromDate() {
        return switch (this) {
            case WEEK_7   -> LocalDateTime.now().minusDays(7);
            case DAY_30   -> LocalDateTime.now().minusDays(30);
            case ALL_TIME -> LocalDateTime.of(1970, 1, 1, 0, 0);
        };
    }
}