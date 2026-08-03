package com.lottery.checker.repository;

import com.lottery.checker.entity.CheckHistory;
import com.lottery.checker.entity.CheckSession;
import com.lottery.checker.entity.LotteryResult;
import com.lottery.checker.entity.LotteryStation;
import com.lottery.checker.entity.Role;
import com.lottery.checker.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.test.context.TestPropertySource;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@TestPropertySource(properties = {
        "spring.config.import=",
        "spring.datasource.url=jdbc:h2:mem:historyrepo;MODE=MySQL;DB_CLOSE_DELAY=-1",
        "spring.datasource.driver-class-name=org.h2.Driver",
        "spring.datasource.username=sa",
        "spring.datasource.password=***",
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "spring.jpa.database-platform=org.hibernate.dialect.H2Dialect"
})
class CheckHistoryRepositoryTest {

    @Autowired
    private CheckHistoryRepository checkHistoryRepository;

    @Autowired
    private CheckSessionRepository checkSessionRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private LotteryResultRepository lotteryResultRepository;

    @Autowired
    private LotteryStationRepository lotteryStationRepository;

    private User user;
    private LotteryResult result;

    @BeforeEach
    void setUp() {
        checkHistoryRepository.deleteAll();
        checkSessionRepository.deleteAll();
        lotteryResultRepository.deleteAll();
        lotteryStationRepository.deleteAll();
        userRepository.deleteAll();

        user = userRepository.save(User.builder()
                .userCode("USR-07-2026-00000001")
                .email("khach1@gmail.com")
                .fullName("Le Van Tam")
                .role(Role.ROLE_USER)
                .isActive(true)
                .build());

        LotteryStation station = lotteryStationRepository.save(LotteryStation.builder()
                .stationCode("SOU-HCM")
                .name("TP. Ho Chi Minh")
                .region("SOUTH")
                .build());

        result = lotteryResultRepository.save(LotteryResult.builder()
                .resultCode("RES-HCM-22072026")
                .station(station)
                .drawDate(LocalDate.of(2026, 7, 22))
                .status("PUBLISH")
                .totalQueries(0L)
                .build());

        CheckSession session = checkSessionRepository.save(CheckSession.builder()
                .user(user)
                .totalSpent(10000L)
                .totalWon(0L)
                .build());

        checkHistoryRepository.save(CheckHistory.builder()
                .session(session)
                .result(result)
                .ticketNumber("123485")
                .isWon(false)
                .wonAmount(0L)
                .build());
    }

    @Test
    void findExistingTickets_DuplicateExists_ReturnsMatchingNumbers() {
        List<String> duplicates = checkHistoryRepository.findExistingTickets(
                user.getId(),
                result.getId(),
                List.of("123485", "999999")
        );

        assertThat(duplicates).containsExactly("123485");
    }

    @Test
    void findExistingTickets_NoDuplicates_ReturnsEmptyList() {
        List<String> duplicates = checkHistoryRepository.findExistingTickets(
                user.getId(),
                result.getId(),
                List.of("111111", "999999")
        );

        assertThat(duplicates).isEmpty();
    }
}