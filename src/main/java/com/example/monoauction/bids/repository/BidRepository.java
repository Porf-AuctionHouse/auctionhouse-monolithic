package com.example.monoauction.bids.repository;

import com.example.monoauction.bids.model.Bid;
import com.example.monoauction.common.enums.BidStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface BidRepository extends JpaRepository<Bid, Long> {

    List<Bid> findByItemIdOrderByAmountDesc(Long itemId);

    List<Bid> findByItemIdOrderByBidTimeDesc(Long itemId);

    Optional<Bid> findTopByItemIdOrderByAmountDesc(Long itemId);

    List<Bid> findByBidderIdOrderByBidTimeDesc(Long bidderId);

    Optional<Bid> findTopByItemIdAndBidderId(Long itemId, Long bidderId);

    List<Bid> findByItemIdAndStatus(Long itemId, BidStatus status);

    List<Bid> findByItemIdAndStatusNot(Long itemId, BidStatus status);

    Long countByItemId(Long itemId);

    Long countByBidderId(Long bidderId);

    List<Bid> findByBidderIdAndStatus(Long bidderId, BidStatus status);


    // Count total bids in batch
    @Query("SELECT COUNT(b) FROM Bid b JOIN AuctionItem ai ON b.itemId = ai.id WHERE ai.batchId = :batchId")
    Long countBidsByBatchId(@Param("batchId") Long batchId);

    // Count unique bidders in batch
    @Query("SELECT COUNT(DISTINCT b.bidderId) FROM Bid b JOIN AuctionItem ai ON b.itemId = ai.id WHERE ai.batchId = :batchId")
    Long countUniqueBiddersByBatchId(@Param("batchId") Long batchId);

    // Average bids per item
    @Query("SELECT AVG(bidCount) FROM (" +
            "SELECT COUNT(b) as bidCount FROM Bid b " +
            "JOIN AuctionItem ai ON b.itemId = ai.id " +
            "WHERE ai.batchId = :batchId " +
            "GROUP BY b.itemId" +
            ")")
    Double getAverageBidsPerItem(@Param("batchId") Long batchId);

    // Top bidders (most active)
    @Query("SELECT b.bidderId, COUNT(b), COUNT(DISTINCT b.itemId) " +
            "FROM Bid b " +
            "GROUP BY b.bidderId " +
            "ORDER BY COUNT(b) DESC")
    List<Object[]> getTopBidders(@Param("limit") int limit);

    // Buyer statistics (winners only)
    @Query("SELECT ai.winnerId, COUNT(ai), SUM(ai.currentBid), AVG(ai.currentBid) " +
            "FROM AuctionItem ai " +
            "WHERE ai.status = 'SOLD' AND ai.winnerId IS NOT NULL " +
            "GROUP BY ai.winnerId " +
            "ORDER BY SUM(ai.currentBid) DESC")
    List<Object[]> getBuyerStatistics(@Param("limit") int limit);

}
