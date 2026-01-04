package com.example.monoauction.batch.repository;

import com.example.monoauction.batch.model.AuctionBatch;
import com.example.monoauction.common.enums.BatchStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface AuctionBatchRepository extends JpaRepository<AuctionBatch, Long> {

    Optional<AuctionBatch> findByWeekNumberAndYear(Integer weekNumber, Integer year);

    Optional<AuctionBatch> findByBatchCode(String batchCode);

    List<AuctionBatch> findByStatus(BatchStatus status);

    List<AuctionBatch> findTop10ByOrderByCreatedAtDesc();

    Page<AuctionBatch> findAllByOrderByCreatedAtDesc(Pageable pageable);


}
