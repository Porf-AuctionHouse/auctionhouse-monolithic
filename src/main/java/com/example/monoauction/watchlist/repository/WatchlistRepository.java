package com.example.monoauction.watchlist.repository;

import com.example.monoauction.watchlist.model.WatchlistItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface WatchlistRepository extends JpaRepository<WatchlistItem, Long> {

    @Query("SELECT w FROM WatchlistItem w " +
            "JOIN FETCH w.auctionItem ai " +
            "WHERE w.user.id = :userId " +
            "ORDER BY w.addedAt DESC")
    List<WatchlistItem> findByUserId(@Param("userId") Long userId);

    @Query("SELECT w FROM WatchlistItem w " +
            "JOIN FETCH w.auctionItem ai " +
            "WHERE w.user.id = :userId AND ai.status = :status " +
            "ORDER BY w.addedAt DESC")
    List<WatchlistItem> findByUserIdAndItemStatus(
            @Param("userId") Long userId,
            @Param("status") String status
    );

    boolean existsByUserIdAndAuctionItemId(Long userId, Long auctionItemId);
    Optional<WatchlistItem> findByUserIdAndAuctionItemId(Long userId, Long auctionItemId);
    long countByUserId(Long userId);

    @Query("SELECT w FROM WatchlistItem w " +
            "JOIN FETCH w.user " +
            "WHERE w.auctionItem.id = :itemId AND w.notifyOnBid = true")
    List<WatchlistItem> findUsersWatchingItem(@Param("itemId") Long itemId);

    void deleteByUserIdAndAuctionItemId(Long userId, Long auctionItemId);


}
