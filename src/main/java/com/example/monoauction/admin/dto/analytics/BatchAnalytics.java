package com.example.monoauction.admin.dto.analytics;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BatchAnalytics {
    private Long batchId;
    private String batchCode;
    private Integer weekNumber;
    private Integer year;
    private String status;

    private LocalDateTime submissionStartDate;
    private LocalDateTime submissionEndDate;
    private LocalDateTime reviewStartDate;
    private LocalDateTime reviewEndDate;
    private LocalDateTime auctionStartTime;
    private LocalDateTime auctionEndTime;

    private Integer totalItemsSubmitted;
    private Integer totalItemsApproved;
    private Integer totalItemsRejected;
    private Integer totalItemsSold;
    private Integer totalItemsUnsold;

    private BigDecimal totalRevenue;
    private BigDecimal averageSalePrice;
    private BigDecimal highestSale;
    private BigDecimal lowestSale;

    private Long totalBids;
    private Long uniqueBidders;
    private Double averageBidsPerItem;

    private Double approvalRate;
    private Double soldRate;
    private Double conversionRate; // Sold / Submitted
}
