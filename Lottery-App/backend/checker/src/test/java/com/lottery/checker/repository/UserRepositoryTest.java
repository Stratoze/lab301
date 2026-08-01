package com.lottery.checker.repository;

import com.lottery.checker.entity.Role;
import com.lottery.checker.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.TestPropertySource;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@TestPropertySource(properties = {
        "spring.config.import=",
        "spring.datasource.url=jdbc:h2:mem:userrepo;MODE=MySQL;DB_CLOSE_DELAY=-1",
        "spring.datasource.driver-class-name=org.h2.Driver",
        "spring.datasource.username=sa",
        "spring.datasource.password=***",
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "spring.jpa.database-platform=org.hibernate.dialect.H2Dialect"
})
class UserRepositoryTest {

    @Autowired
    private UserRepository userRepository;

    private User user;

    @BeforeEach
    void setUp() {
        userRepository.deleteAll();

        user = User.builder()
                .userCode("USR-07-2026-00000001")
                .email("khach1@gmail.com")
                .phone("0910000001")
                .password("$2a$12$hashedpassword...")
                .fullName("Le Van Tam")
                .role(Role.ROLE_USER)
                .isActive(true)
                .lastLogin(LocalDateTime.now())
                .build();

        userRepository.save(user);
    }

    @Test
    void findByEmail_ExistingEmail_ReturnsUser() {
        Optional<User> found = userRepository.findByEmail("khach1@gmail.com");

        assertThat(found).isPresent();
        assertThat(found.get().getFullName()).isEqualTo("Le Van Tam");
    }

    @Test
    void findByPhone_ExistingPhone_ReturnsUser() {
        Optional<User> found = userRepository.findByPhone("0910000001");

        assertThat(found).isPresent();
        assertThat(found.get().getEmail()).isEqualTo("khach1@gmail.com");
    }

    @Test
    void searchUsers_ByFullNameKeyword_ReturnsMatchingUsers() {
        Page<User> result = userRepository.searchUsers("Le Van", PageRequest.of(0, 20));

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getEmail()).isEqualTo("khach1@gmail.com");
    }

    @Test
    void searchUsers_ByEmailKeyword_ReturnsMatchingUsers() {
        Page<User> result = userRepository.searchUsers("khach1", PageRequest.of(0, 20));

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getEmail()).isEqualTo("khach1@gmail.com");
    }

    @Test
    void countUsersByMonth_MatchingUserCodePrefix_ReturnsCount() {
        long count = userRepository.countUsersByMonth("07-2026");

        assertThat(count).isEqualTo(1L);
    }
}