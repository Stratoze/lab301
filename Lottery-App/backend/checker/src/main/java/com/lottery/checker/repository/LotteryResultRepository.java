package com.lottery.checker.repository;

import com.lottery.checker.entity.LotteryResult;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.Optional;

@Repository
public interface LotteryResultRepository extends JpaRepository<LotteryResult, Long> {
    
    Optional<LotteryResult> findByResultCode(String resultCode);

    @Query("SELECT r FROM LotteryResult r WHERE " +
           "(:stationId IS NULL OR r.station.id = :stationId) AND " +
           "(:startDate IS NULL OR r.drawDate >= :startDate) AND " +
           "(:endDate IS NULL OR r.drawDate <= :endDate) AND " +
           "(:keyword IS NULL OR LOWER(r.resultCode) LIKE LOWER(CONCAT('%', :keyword, '%')))")
    Page<LotteryResult> searchTickets(
            @Param("stationId") Integer stationId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate,
            @Param("keyword") String keyword,
            Pageable pageable);
}