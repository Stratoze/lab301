package com.lottery.checker.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "lottery_results", uniqueConstraints = {
    @UniqueConstraint(name = "unique_idx_station_date", columnNames = {"station_id", "draw_date"})
}, indexes = {
    @Index(name = "idx_draw_date", columnList = "draw_date"),
    @Index(name = "idx_status", columnList = "status")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LotteryResult {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "result_code", nullable = false, unique = true, length = 30)
    private String resultCode;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "station_id", nullable = false)
    private LotteryStation station;

    @Column(name = "draw_date", nullable = false)
    private LocalDate drawDate;

    @Column(length = 20)
    @Builder.Default
    private String status = "UNPUBLISH"; // UNPUBLISH, PUBLISH

    @Column(name = "total_queries")
    @Builder.Default
    private Long totalQueries = 0L;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by")
    private User createdBy;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "published_by")
    private User publishedBy;

    @Column(name = "published_at")
    private LocalDateTime publishedAt;

    @OneToMany(mappedBy = "result", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<PrizeDetail> prizeDetails = new ArrayList<>();

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public void addPrizeDetail(PrizeDetail detail) {
        prizeDetails.add(detail);
        detail.setResult(this);
    }
}