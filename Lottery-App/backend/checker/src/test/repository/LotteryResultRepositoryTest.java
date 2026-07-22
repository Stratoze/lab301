package com.lottery.checker.repository;

import com.lottery.checker.entity.LotteryResult;
import com.lottery.checker.entity.LotteryStation;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.TestPropertySource;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@TestPropertySource(properties = {
        "spring.config.import=",
        "spring.datasource.url=jdbc:h2:mem:resultrepo;MODE=MySQL;DB_CLOSE_DELAY=-1",
        "spring.datasource.driver-class-name=org.h2.Driver",
        "spring.datasource.username=sa",
        "spring.datasource.password=***",
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "spring.jpa.database-platform=org.hibernate.dialect.H2Dialect"
})
class LotteryResultRepositoryTest {

    @Autowired
    private LotteryResultRepository lotteryResultRepository;

    @Autowired
    private LotteryStationRepository lotteryStationRepository;

    private LotteryStation station;
    private LocalDate drawDate;

    @BeforeEach
    void setUp() {
        lotteryResultRepository.deleteAll();
        lotteryStationRepository.deleteAll();

        station = LotteryStation.builder()
                .stationCode("SOU-HCM")
                .name("TP. Ho Chi Minh")
                .region("SOUTH")
                .build();

        station = lotteryStationRepository.save(station);
        drawDate = LocalDate.of(2026, 7, 22);

        LotteryResult result = LotteryResult.builder()
                .resultCode("RES-HCM-22072026")
                .station(station)
                .drawDate(drawDate)
                .status("PUBLISH")
                .totalQueries(0L)
                .build();

        lotteryResultRepository.save(result);
    }

    @Test
    void findByResultCode_ExistingCode_ReturnsResult() {
        Optional<LotteryResult> found = lotteryResultRepository.findByResultCode("RES-HCM-22072026");

        assertThat(found).isPresent();
        assertThat(found.get().getStation().getName()).isEqualTo("TP. Ho Chi Minh");
    }

    @Test
    void findByStationIdAndDrawDate_ExistingResult_ReturnsResult() {
        Optional<LotteryResult> found =
                lotteryResultRepository.findByStationIdAndDrawDate(station.getId(), drawDate);

        assertThat(found).isPresent();
        assertThat(found.get().getResultCode()).isEqualTo("RES-HCM-22072026");
    }

    @Test
    void searchTickets_ByStationAndDateRange_ReturnsResult() {
        Page<LotteryResult> page = lotteryResultRepository.searchTickets(
                station.getId(),
                drawDate.minusDays(1),
                drawDate.plusDays(1),
                null,
                PageRequest.of(0, 10)
        );

        assertThat(page.getContent()).hasSize(1);
        assertThat(page.getContent().get(0).getResultCode()).isEqualTo("RES-HCM-22072026");
    }

    @Test
    void findDrawDatesByStation_PublishedResult_ReturnsDrawDate() {
        List<LocalDate> dates = lotteryResultRepository.findDrawDatesByStation(station.getId());

        assertThat(dates).containsExactly(drawDate);
    }
}