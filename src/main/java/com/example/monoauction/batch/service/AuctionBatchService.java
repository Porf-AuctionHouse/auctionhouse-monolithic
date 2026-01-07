package com.example.monoauction.batch.service;

import com.example.monoauction.batch.model.AuctionBatch;
import com.example.monoauction.batch.repository.AuctionBatchRepository;
import com.example.monoauction.common.enums.BatchStatus;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.TemporalAdjusters;
import java.time.temporal.WeekFields;
import java.util.Optional;

@Service
@Transactional
@RequiredArgsConstructor
public class AuctionBatchService {
    private final AuctionBatchRepository batchRepository;

    public AuctionBatch getCurrentBatch(){
        LocalDate now = LocalDate.now();
        int week = now.get(WeekFields.of(DayOfWeek.MONDAY, 1).weekOfYear());
        int year = now.getYear();

        Optional<AuctionBatch> existingBatch = batchRepository.findByWeekNumberAndYear(week, year);

        if(existingBatch.isPresent()){
            return existingBatch.get();
        } else {
            return createNewBatch(week, year);
        }
    }

    public AuctionBatch createNewBatch(int week, int year){
        LocalDate monday = LocalDate.now().with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));

        AuctionBatch batch = new AuctionBatch();
        batch.setBatchCode("BATCH-" + year + "-W" + String.format("%02d", week));
        batch.setWeekNumber(week);
        batch.setYear(year);
        batch.setStatus(BatchStatus.SUBMISSION);

        batch.setSubmissionStartDate(monday.atStartOfDay());
        batch.setSubmissionEndDate(monday.plusDays(2).atTime(23,59,59));

        batch.setReviewStartDate(monday.plusDays(3).atStartOfDay());
        batch.setReviewEndDate(monday.plusDays(4).atTime(23,59,59));

        batch.setAuctionStartTime(monday.plusDays(5).atTime(10,0));
        batch.setAuctionEndTime(monday.plusDays(6).atTime(20,0));

        batch.setTotalItemsSubmitted(0);
        batch.setTotalItemsApproved(0);
        batch.setTotalItemsRejected(0);
        batch.setTotalItemsSold(0);
        batch.setTotalRevenue(BigDecimal.ZERO);

        return batchRepository.save(batch);

    }

    public AuctionBatch getBatchById(Long batchId){
        return batchRepository.findById(batchId).orElseThrow(
                () -> new RuntimeException("Batch Not Found With These Details"));
    }

    public AuctionBatch getBatchByCode(String batchCode){
        return batchRepository.findByBatchCode(batchCode).orElseThrow(
                () -> new RuntimeException("Batch Not Found With These Details"));
    }

    public Page<AuctionBatch> getAllBatches(Pageable pageable){
        return batchRepository.findAllByOrderByCreatedAtDesc(pageable);
    }

    public boolean isSubmissionOpen(){
        AuctionBatch currentBatch = getCurrentBatch();
        LocalDateTime now = LocalDateTime.now();

        return currentBatch.getStatus() == BatchStatus.SUBMISSION &&
                now.isAfter(currentBatch.getSubmissionStartDate()) &&
                now.isBefore(currentBatch.getSubmissionEndDate());
    }

    public boolean isReviewPhaseActive(){
        AuctionBatch currentBatch = getCurrentBatch();
        LocalDateTime now = LocalDateTime.now();

        return currentBatch.getStatus() == BatchStatus.REVIEW &&
                now.isAfter(currentBatch.getReviewStartDate()) &&
                now.isBefore(currentBatch.getReviewEndDate());
    }

    public boolean isAuctionLive(){
        AuctionBatch currentBatch = getCurrentBatch();
        return currentBatch.getStatus() == BatchStatus.LIVE;
    }

    public void updateBatchStatus(Long batchId, BatchStatus newStatus){
        AuctionBatch batch = getBatchById(batchId);
        batch.setStatus(newStatus);
        batchRepository.save(batch);
    }

    public void incrementItemSubmitted(Long batchId){
        AuctionBatch batch = getBatchById(batchId);
        batch.setTotalItemsSubmitted(batch.getTotalItemsSubmitted() + 1);
        batchRepository.save(batch);
    }

    public void incrementItemApproved(Long batchId){
        AuctionBatch batch = getBatchById(batchId);
        batch.setTotalItemsApproved(batch.getTotalItemsApproved() + 1);
        batchRepository.save(batch);
    }

    public void incrementItemRejected(Long batchId){
        AuctionBatch batch = getBatchById(batchId);
        batch.setTotalItemsRejected(batch.getTotalItemsRejected() + 1);
        batchRepository.save(batch);
    }

    public void incrementItemSold(Long batchId, BigDecimal amount){
        AuctionBatch batch = getBatchById(batchId);
        batch.setTotalItemsSold(batch.getTotalItemsSold() + 1);
        batch.setTotalRevenue(batch.getTotalRevenue().add(amount));
        batchRepository.save(batch);
    }
}
