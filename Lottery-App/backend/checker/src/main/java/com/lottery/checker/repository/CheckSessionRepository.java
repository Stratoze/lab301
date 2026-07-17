package com.lottery.checker.repository;

import com.lottery.checker.entity.CheckSession;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface CheckSessionRepository extends JpaRepository<CheckSession, Long> {
    List<CheckSession> findAllByUserIdOrderByCreatedAtDesc(Long userId);
}