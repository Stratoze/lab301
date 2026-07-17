package com.lottery.checker.repository;

import com.lottery.checker.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    
    Optional<User> findByEmail(String email);
    
    Optional<User> findByUserCode(String userCode);

    @Query("SELECT u FROM User u WHERE " +
           "LOWER(u.fullName) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(u.email) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "u.phone LIKE CONCAT('%', :keyword, '%') OR " +
           "u.userCode LIKE CONCAT('%', :keyword, '%')")
    Page<User> searchUsers(@Param("keyword") String keyword, Pageable pageable);

    @Query("SELECT COUNT(u) FROM User u WHERE u.userCode LIKE CONCAT('USR-', :monthYear, '-%')")
    long countUsersByMonth(@Param("monthYear") String monthYear);
}