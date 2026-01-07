package com.example.monoauction.batch.dto;

import com.example.monoauction.batch.model.AuctionBatch;
import com.example.monoauction.common.enums.BatchStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class BatchResponse {
    private Long id;
    private String batchCode;
    private Integer weekNumber;
    private Integer year;
    private BatchStatus status;
    private LocalDateTime submissionStartDate;
    private LocalDateTime submissionEndDate;
    private LocalDateTime reviewStartDate;
    private LocalDateTime reviewEndDate;
    private LocalDateTime auctionStartDate;
    private LocalDateTime auctionEndDate;
    private Integer totalItemsSubmitted;
    private Integer totalItemsApproved;
    private Integer totalItemsRejected;
    private Integer totalItemsSold;
    private BigDecimal totalRevenue;
    private LocalDateTime createdAt;

    public BatchResponse(AuctionBatch batch){
        this.id = batch.getId();
        this.batchCode = batch.getBatchCode();
        this.weekNumber = batch.getWeekNumber();
        this.year = batch.getYear();
        this.status = batch.getStatus();
        this.submissionStartDate = batch.getSubmissionStartDate();
        this.submissionEndDate = batch.getSubmissionEndDate();
        this.reviewStartDate = batch.getReviewStartDate();
        this.reviewEndDate = batch.getReviewEndDate();
        this.auctionStartDate = batch.getAuctionStartTime();
        this.auctionEndDate = batch.getAuctionEndTime();
        this.totalItemsSubmitted = batch.getTotalItemsSubmitted();
        this.totalItemsApproved = batch.getTotalItemsApproved();
        this.totalItemsRejected = batch.getTotalItemsRejected();
        this.totalItemsSold = batch.getTotalItemsSold();
        this.totalRevenue = batch.getTotalRevenue();
        this.createdAt = batch.getCreatedAt();
    }
}
