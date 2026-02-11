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
public class DashboardOverview {
    // Current Batch Stats
    private String currentBatchCode;
    private String currentBatchStatus;
    private LocalDateTime batchStartDate;
    private LocalDateTime auctionStartTime;
    private LocalDateTime auctionEndTime;

    // Item Statistics
    private Integer totalItemsSubmitted;
    private Integer totalItemsApproved;
    private Integer totalItemsRejected;
    private Integer totalItemsLive;
    private Integer totalItemsSold;
    private Integer totalItemsUnsold;

    // Financial Metrics
    private BigDecimal totalRevenue;
    private BigDecimal totalRevenueThisBatch;
    private BigDecimal averageItemPrice;
    private BigDecimal highestBid;

    // User Metrics
    private Long totalUsers;
    private Long activeUsers;
    private Long totalSellers;
    private Long totalBuyers;
    private Long newUsersThisWeek;

    // Bidding Metrics
    private Long totalBids;
    private Long totalBidsThisBatch;
    private Long activeBidders;
    private Double averageBidsPerItem;

    // Performance Indicators
    private Double itemApprovalRate; // Approved / Submitted
    private Double itemSoldRate; // Sold / Live
    private Double userEngagementRate; // Active / Total
}
