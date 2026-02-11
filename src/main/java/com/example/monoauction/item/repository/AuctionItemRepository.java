package com.example.monoauction.item.repository;

import com.example.monoauction.common.enums.ItemCategory;
import com.example.monoauction.common.enums.ItemStatus;
import com.example.monoauction.item.model.AuctionItem;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.util.List;

public interface AuctionItemRepository extends JpaRepository<AuctionItem, Long> , JpaSpecificationExecutor<AuctionItem>
{

    List<AuctionItem> findByBatchId(Long batchId);

    List<AuctionItem> findBySellerIdOrderByCreatedAtDesc(Long sellerId);

    Page<AuctionItem> findByStatus(ItemStatus status, Pageable pageable);

    List<AuctionItem> findByBatchIdAndStatus(Long batchId, ItemStatus status);

    List<AuctionItem> findByBatchIdAndStatusIn(Long batchId, List<ItemStatus> status);

    List<AuctionItem> findByStatusOrderByCreatedAtDesc(ItemStatus status);

    List<AuctionItem> findByCategoryAndStatus(ItemCategory category, ItemStatus status);

    Long countBySellerId(Long sellerId);

    Long countByBatchIdAndStatus(Long batchId, ItemStatus status);

    List<AuctionItem> findByTitleContainingIgnoreCaseAndStatus(String title, ItemStatus status);

    List<AuctionItem> findByWinnerIdOrderByCreatedAtDesc(Long winnerId);

    Page<AuctionItem> findByCategory(ItemCategory category, Pageable pageable);

    List<AuctionItem> findTop10ByTitleStartingWithIgnoreCase(String prefix);

    @Query("SELECT i FROM AuctionItem i WHERE " +
            "LOWER(i.title) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(i.description) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    Page<AuctionItem> searchByKeyword(@Param("keyword") String keyword, Pageable pageable);

    @Query("SELECT i FROM AuctionItem i WHERE " +
            "(LOWER(i.title) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(i.description) LIKE LOWER(CONCAT('%', :keyword, '%'))) AND " +
            "i.category = :category")
    Page<AuctionItem> searchByKeywordAndCategory(
            @Param("keyword") String keyword,
            @Param("category") ItemCategory category,
            Pageable pageable);

    @Query("SELECT i FROM AuctionItem i WHERE " +
            "i.startingPrice BETWEEN :minPrice AND :maxPrice")
    Page<AuctionItem> findByPriceRange(
            @Param("minPrice") BigDecimal minPrice,
            @Param("maxPrice") BigDecimal maxPrice,
            Pageable pageable);

    Page<AuctionItem> findByCategoryAndStatus(
            ItemCategory category,
            ItemStatus status,
            Pageable pageable);

    @Query("SELECT DISTINCT i.category FROM AuctionItem i ORDER BY i.category")
    List<ItemCategory> findAllCategories();

    @Query("SELECT i.category, COUNT(i) FROM AuctionItem i " +
            "WHERE i.status = :status GROUP BY i.category")
    List<Object[]> countByCategory(@Param("status") ItemStatus status);

    @Query("SELECT MIN(i.startingPrice), MAX(i.startingPrice) FROM AuctionItem i " +
            "WHERE i.status = :status")
    Object[] findPriceRange(@Param("status") ItemStatus status);

    @Query("SELECT i FROM AuctionItem i WHERE i.totalBids > 0")
    Page<AuctionItem> findItemsWithBids(Pageable pageable);

    @Query("SELECT i FROM AuctionItem i WHERE " +
            "(:hasReserve = true AND i.reservePrice IS NOT NULL) OR " +
            "(:hasReserve = false AND i.reservePrice IS NULL)")
    Page<AuctionItem> findByReservePriceExistence(
            @Param("hasReserve") boolean hasReserve,
            Pageable pageable);

    // Category analytics
    @Query("SELECT ai.category, COUNT(ai), " +
            "SUM(CASE WHEN ai.status = 'SOLD' THEN 1 ELSE 0 END), " +
            "COALESCE(SUM(CASE WHEN ai.status = 'SOLD' THEN ai.currentBid ELSE 0 END), 0), " +
            "COALESCE(AVG(CASE WHEN ai.status = 'SOLD' THEN ai.currentBid END), 0), " +
            "COALESCE(MAX(CASE WHEN ai.status = 'SOLD' THEN ai.currentBid END), 0) " +
            "FROM AuctionItem ai " +
            "WHERE ai.batchId = :batchId " +
            "GROUP BY ai.category " +
            "ORDER BY SUM(CASE WHEN ai.status = 'SOLD' THEN ai.currentBid ELSE 0 END) DESC")
    List<Object[]> getCategoryAnalytics(@Param("batchId") Long batchId);

    // Average item price for sold items
    @Query("SELECT COALESCE(AVG(ai.currentBid), 0) FROM AuctionItem ai WHERE ai.status = 'SOLD' AND ai.batchId = :batchId")
    BigDecimal getAverageSalePrice(@Param("batchId") Long batchId);

    // Highest bid in batch
    @Query("SELECT COALESCE(MAX(ai.currentBid), 0) FROM AuctionItem ai WHERE ai.batchId = :batchId")
    BigDecimal getHighestBid(@Param("batchId") Long batchId);

    // Count by status
    @Query("SELECT COUNT(ai) FROM AuctionItem ai WHERE ai.batchId = :batchId AND ai.status = :status")
    Long countByBatchIdAndStatus(@Param("batchId") Long batchId, @Param("status") String status);

    // Top selling items
    @Query("SELECT ai FROM AuctionItem ai WHERE ai.status = 'SOLD' ORDER BY ai.currentBid DESC")
    List<AuctionItem> findTopSellingItems(@Param("limit") int limit);

    // Items by seller with stats
    @Query("SELECT ai.sellerId, COUNT(ai), " +
            "SUM(CASE WHEN ai.status = 'SOLD' THEN 1 ELSE 0 END), " +
            "COALESCE(SUM(CASE WHEN ai.status = 'SOLD' THEN ai.currentBid ELSE 0 END), 0) " +
            "FROM AuctionItem ai " +
            "GROUP BY ai.sellerId " +
            "ORDER BY SUM(CASE WHEN ai.status = 'SOLD' THEN ai.currentBid ELSE 0 END) DESC")
    List<Object[]> getSellerStatistics(@Param("limit") int limit);
}
