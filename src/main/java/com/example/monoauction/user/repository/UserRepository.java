package com.example.monoauction.user.repository;

import com.example.monoauction.common.enums.UserRole;
import com.example.monoauction.user.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmail(String email);

    Boolean existsByEmail(String email);

    List<User> findByRole(UserRole role);

    List<User> findByIsActiveTrue();

    // Count active users
    Long countByIsActiveTrue();

    // Count users by role
    Long countByRole(String role);

    @Query("SELECT u FROM User u WHERE u.id = :userId")
    Optional<User> findById(@Param("userId") Long userId);

    // New users in time period
    @Query("SELECT COUNT(u) FROM User u WHERE u.createdAt >= :startDate AND u.createdAt <= :endDate")
    Long countNewUsers(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);

    // Active users (logged in recently or placed bids/items)
    @Query("SELECT COUNT(DISTINCT u.id) FROM User u " +
            "LEFT JOIN Bid b ON u.id = b.bidderId " +
            "LEFT JOIN AuctionItem ai ON u.id = ai.sellerId " +
            "WHERE u.lastLoginAt >= :since OR b.createdAt >= :since OR ai.createdAt >= :since")
    Long countActiveUsersSince(@Param("since") LocalDateTime since);
}
