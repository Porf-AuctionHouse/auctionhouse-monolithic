package com.example.monoauction.batch.repository;

import com.example.monoauction.batch.model.AuctionBatch;
import com.example.monoauction.common.enums.BatchStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

public interface AuctionBatchRepository extends JpaRepository<AuctionBatch, Long> {

    Optional<AuctionBatch> findByWeekNumberAndYearAndIsDeleted(Integer weekNumber, Integer year, Boolean isDeleted);

    Optional<AuctionBatch> findByBatchCode(String batchCode);

    List<AuctionBatch> findByStatus(BatchStatus status);

    List<AuctionBatch> findTop10ByOrderByCreatedAtDesc();

    Page<AuctionBatch> findAllByOrderByCreatedAtDesc(Pageable pageable);

    // Get current active batch
    @Query("SELECT ab FROM AuctionBatch ab WHERE ab.status IN ('SUBMISSION', 'REVIEW', 'LIVE') ORDER BY ab.createdAt DESC")
    Optional<AuctionBatch> findCurrentBatch();

    // Get batches for analytics (last N batches)
    @Query("SELECT ab FROM AuctionBatch ab ORDER BY ab.year DESC, ab.weekNumber DESC")
    List<AuctionBatch> findRecentBatches(@Param("limit") int limit);

    // Total revenue across all batches
    @Query("SELECT COALESCE(SUM(ab.totalRevenue), 0) FROM AuctionBatch ab WHERE ab.status = 'SETTLED'")
    BigDecimal getTotalRevenue();

    // Revenue for specific batch
    @Query("SELECT COALESCE(ab.totalRevenue, 0) FROM AuctionBatch ab WHERE ab.id = :batchId")
    BigDecimal getBatchRevenue(@Param("batchId") Long batchId);
}
