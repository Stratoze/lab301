package com.lottery.checker.repository;

import com.lottery.checker.entity.LotteryResult;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface LotteryResultRepository extends JpaRepository<LotteryResult, Long> {

    Optional<LotteryResult> findByResultCode(String resultCode);

    Optional<LotteryResult> findByStationIdAndDrawDate(Integer stationId, LocalDate drawDate);

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
            Pageable pageable
    );

    @Query("SELECT DISTINCT r.drawDate FROM LotteryResult r WHERE r.station.id = :stationId AND r.status = 'PUBLISH' ORDER BY r.drawDate")
    List<LocalDate> findDrawDatesByStation(@Param("stationId") Integer stationId);

    @Modifying
    @Query("UPDATE LotteryResult r SET r.totalQueries = r.totalQueries + 1 WHERE r.id = :id")
    void incrementTotalQueries(@Param("id") Long id);
}