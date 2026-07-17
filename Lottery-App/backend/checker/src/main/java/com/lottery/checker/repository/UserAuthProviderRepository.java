package com.lottery.checker.repository;

import com.lottery.checker.entity.User;
import com.lottery.checker.entity.UserAuthProvider;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface UserAuthProviderRepository extends JpaRepository<UserAuthProvider, Long> {
    Optional<UserAuthProvider> findByProviderAndProviderId(String provider, String providerId);
    List<UserAuthProvider> findByUser(User user);
    boolean existsByUserAndProvider(User user, String provider);
}