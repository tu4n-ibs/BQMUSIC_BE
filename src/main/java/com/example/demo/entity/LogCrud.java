package com.example.demo.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class LogCrud {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;
    private String action;
    private String userId;
    private LocalDateTime time;
    @Column(columnDefinition = "jsonb")
    private String oldData;

    @Column(columnDefinition = "jsonb")
    private String newData;
}
