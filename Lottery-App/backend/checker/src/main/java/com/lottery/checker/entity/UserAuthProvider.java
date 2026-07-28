package com.lottery.checker.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "user_auth_providers", uniqueConstraints = {
    @UniqueConstraint(name = "uq_provider_account", columnNames = {"provider", "provider_id"}),
    @UniqueConstraint(name = "uq_user_provider",    columnNames = {"user_id", "provider"})
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserAuthProvider {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private
    User user;

    @Column(nullable = false, length = 20)
    private String provider; // 'GOOGLE', 'FACEBOOK'

    @Column(name = "provider_id", nullable = false, length = 255)
    private String providerId;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
}