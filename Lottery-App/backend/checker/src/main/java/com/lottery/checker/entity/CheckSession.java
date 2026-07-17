package com.lottery.checker.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "check_sessions")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CheckSession {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user; // Null if Guest

    @Column(name = "total_spent")
    @Builder.Default
    private Long totalSpent = 0L;

    @Column(name = "total_won")
    @Builder.Default
    private Long totalWon = 0L;

    @OneToMany(mappedBy = "session", cascade = CascadeType.ALL)
    @Builder.Default
    private List<CheckHistory> histories = new ArrayList<>();

    @CreationTimestamp
    @Column(name = "created_at")
    private LocalDateTime createdAt;
}