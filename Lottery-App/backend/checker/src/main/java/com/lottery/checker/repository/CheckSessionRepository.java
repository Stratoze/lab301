package com.lottery.checker.repository;

import com.lottery.checker.entity.CheckSession;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface CheckSessionRepository extends JpaRepository<CheckSession, Long> {

    @Query("SELECT s FROM CheckSession s " +
       "LEFT JOIN FETCH s.histories h " +
       "LEFT JOIN FETCH h.result r " +
       "LEFT JOIN FETCH r.station " +
       "WHERE s.user.id = :userId ORDER BY s.createdAt DESC")
    List<CheckSession> findAllWithHistoriesByUserId(@Param("userId") Long userId);
}

