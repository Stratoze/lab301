package com.lottery.checker.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "lottery_stations")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LotteryStation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "station_code", nullable = false, unique = true, length = 15)
    private String stationCode;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(nullable = false, length = 20)
    private String region; // 'NORTH', 'CENTRAL', 'SOUTH'
}