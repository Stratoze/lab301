package com.lottery.checker.repository;

import com.lottery.checker.entity.LotteryStation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LotteryStationRepository extends JpaRepository<LotteryStation, Integer> {
    List<LotteryStation> findAllByOrderByRegionAscNameAsc();
}