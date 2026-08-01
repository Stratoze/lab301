package com.lottery.checker.repository;

import com.lottery.checker.entity.CheckHistory;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface CheckHistoryRepository extends JpaRepository<CheckHistory, Long> {

    @Query("SELECT COUNT(h) > 0 FROM CheckHistory h " +
           "JOIN h.session s " +
           "WHERE s.user.id = :userId " +
           "AND h.result.id = :resultId " +
           "AND h.ticketNumber = :ticketNumber")
    boolean existsByUserAndResultAndTicket(@Param("userId") Long userId,
                                           @Param("resultId") Long resultId,
                                           @Param("ticketNumber") String ticketNumber);

    @Query("SELECT h.ticketNumber FROM CheckHistory h " +
           "JOIN h.session s " +
           "WHERE s.user.id = :userId " +
           "AND h.result.id = :resultId " +
           "AND h.ticketNumber IN :ticketNumbers")
    List<String> findExistingTickets(@Param("userId") Long userId,
                                     @Param("resultId") Long resultId,
                                     @Param("ticketNumbers") List<String> ticketNumbers);
}