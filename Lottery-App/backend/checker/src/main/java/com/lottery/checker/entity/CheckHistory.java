package com.lottery.checker.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "check_histories")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CheckHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "session_id", nullable = false)
    private CheckSession session;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "result_id", nullable = false)
    private LotteryResult result;

    @Column(name = "ticket_number", nullable = false, length = 10)
    private String ticketNumber;

    @Column(name = "is_won")
    @Builder.Default
    private Boolean isWon = false;

    @Column(name = "won_prize", length = 20)
    private String wonPrize;

    @Column(name = "won_amount")
    @Builder.Default
    private Long wonAmount = 0L;

    @CreationTimestamp
    @Column(name = "check_time")
    private LocalDateTime checkTime;
}