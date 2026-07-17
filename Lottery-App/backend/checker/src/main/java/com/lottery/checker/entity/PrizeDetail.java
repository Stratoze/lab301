package com.lottery.checker.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "prize_details", uniqueConstraints = {
    @UniqueConstraint(name = "unique_idx_result_prize_number", columnNames = {"result_id", "prize_type", "winning_number"})
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PrizeDetail {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "result_id", nullable = false)
    private LotteryResult result;

    @Column(name = "prize_type", nullable = false, length = 20)
    private String prizeType; // G8, G7, G_DB etc.

    @Column(name = "winning_number", nullable = false, length = 10)
    private String winningNumber;

    @Column(name = "reward_amount", nullable = false)
    private Long rewardAmount;
}