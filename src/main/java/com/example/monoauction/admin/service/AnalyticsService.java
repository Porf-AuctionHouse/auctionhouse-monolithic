package com.example.monoauction.admin.service;

import com.example.monoauction.admin.dto.analytics.*;
import com.example.monoauction.batch.model.AuctionBatch;
import com.example.monoauction.batch.repository.AuctionBatchRepository;
import com.example.monoauction.bids.repository.BidRepository;
import com.example.monoauction.common.execptions.ResourceNotFoundException;
import com.example.monoauction.item.repository.AuctionItemRepository;
import com.example.monoauction.user.model.User;
import com.example.monoauction.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AnalyticsService {

    private final AuctionBatchRepository batchRepository;
    private final AuctionItemRepository itemRepository;
    private final BidRepository bidRepository;
    private final UserRepository userRepository;

    @Cacheable(value = "dashboardOverview", unless = "#result == null")
    public DashboardOverview getDashboardOverview() {
        log.info("Generating dashboard overview");

        // Get current batch
        AuctionBatch currentBatch = batchRepository.findCurrentBatch()
                .orElseThrow(() -> new ResourceNotFoundException("No active batch found"));

        Long batchId = currentBatch.getId();

        // Item statistics
        Integer totalSubmitted = currentBatch.getTotalItemsSubmitted();
        Integer totalApproved = currentBatch.getTotalItemsApproved();
        Integer totalRejected = currentBatch.getTotalItemsRejected();
        Long totalLive = itemRepository.countByBatchIdAndStatus(batchId, "LIVE");
        Long totalSold = itemRepository.countByBatchIdAndStatus(batchId, "SOLD");
        Long totalUnsold = itemRepository.countByBatchIdAndStatus(batchId, "UNSOLD");

        // Financial metrics
        BigDecimal totalRevenue = batchRepository.getTotalRevenue();
        BigDecimal totalRevenueThisBatch = currentBatch.getTotalRevenue() != null ?
                currentBatch.getTotalRevenue() : BigDecimal.ZERO;
        BigDecimal averageItemPrice = itemRepository.getAverageSalePrice(batchId);
        BigDecimal highestBid = itemRepository.getHighestBid(batchId);

        // User metrics
        Long totalUsers = userRepository.count();
        Long activeUsers = userRepository.countByIsActiveTrue();
        Long totalSellers = userRepository.countByRole("SELLER");
        Long totalBuyers = userRepository.countByRole("BUYER");

        LocalDateTime weekStart = currentBatch.getSubmissionStartDate();
        LocalDateTime now = LocalDateTime.now();
        Long newUsersThisWeek = userRepository.countNewUsers(weekStart, now);

        // Bidding metrics
        Long totalBids = bidRepository.count();
        Long totalBidsThisBatch = bidRepository.countBidsByBatchId(batchId);
        Long activeBidders = bidRepository.countUniqueBiddersByBatchId(batchId);
        Double averageBidsPerItem = bidRepository.getAverageBidsPerItem(batchId);

        // Calculate rates
        Double approvalRate = totalSubmitted > 0 ?
                (totalApproved.doubleValue() / totalSubmitted.doubleValue()) * 100 : 0.0;
        Double soldRate = totalLive > 0 ?
                (totalSold.doubleValue() / totalLive.doubleValue()) * 100 : 0.0;
        Double engagementRate = totalUsers > 0 ?
                (activeUsers.doubleValue() / totalUsers.doubleValue()) * 100 : 0.0;

        return DashboardOverview.builder()
                .currentBatchCode(currentBatch.getBatchCode())
                .currentBatchStatus(currentBatch.getStatus().name())
                .batchStartDate(currentBatch.getSubmissionStartDate())
                .auctionStartTime(currentBatch.getAuctionStartTime())
                .auctionEndTime(currentBatch.getAuctionEndTime())
                .totalItemsSubmitted(totalSubmitted)
                .totalItemsApproved(totalApproved)
                .totalItemsRejected(totalRejected)
                .totalItemsLive(totalLive.intValue())
                .totalItemsSold(totalSold.intValue())
                .totalItemsUnsold(totalUnsold.intValue())
                .totalRevenue(totalRevenue)
                .totalRevenueThisBatch(totalRevenueThisBatch)
                .averageItemPrice(averageItemPrice)
                .highestBid(highestBid)
                .totalUsers(totalUsers)
                .activeUsers(activeUsers)
                .totalSellers(totalSellers)
                .totalBuyers(totalBuyers)
                .newUsersThisWeek(newUsersThisWeek)
                .totalBids(totalBids)
                .totalBidsThisBatch(totalBidsThisBatch)
                .activeBidders(activeBidders)
                .averageBidsPerItem(averageBidsPerItem != null ? averageBidsPerItem : 0.0)
                .itemApprovalRate(approvalRate)
                .itemSoldRate(soldRate)
                .userEngagementRate(engagementRate)
                .build();
    }

    public BatchAnalytics getBatchAnalytics(Long batchId) {
        AuctionBatch batch = batchRepository.findById(batchId)
                .orElseThrow(() -> new ResourceNotFoundException("Batch not found"));

        Long totalSold = itemRepository.countByBatchIdAndStatus(batchId, "SOLD");
        Long totalUnsold = itemRepository.countByBatchIdAndStatus(batchId, "UNSOLD");

        BigDecimal averagePrice = itemRepository.getAverageSalePrice(batchId);

        Long totalBids = bidRepository.countBidsByBatchId(batchId);
        Long uniqueBidders = bidRepository.countUniqueBiddersByBatchId(batchId);
        Double avgBidsPerItem = bidRepository.getAverageBidsPerItem(batchId);

        Double approvalRate = batch.getTotalItemsSubmitted() > 0 ?
                (batch.getTotalItemsApproved().doubleValue() / batch.getTotalItemsSubmitted().doubleValue()) * 100 : 0.0;
        Double soldRate = (totalSold + totalUnsold) > 0 ?
                (totalSold.doubleValue() / (totalSold + totalUnsold)) * 100 : 0.0;
        Double conversionRate = batch.getTotalItemsSubmitted() > 0 ?
                (totalSold.doubleValue() / batch.getTotalItemsSubmitted().doubleValue()) * 100 : 0.0;

        return BatchAnalytics.builder()
                .batchId(batch.getId())
                .batchCode(batch.getBatchCode())
                .weekNumber(batch.getWeekNumber())
                .year(batch.getYear())
                .status(batch.getStatus().name())
                .submissionStartDate(batch.getSubmissionStartDate())
                .submissionEndDate(batch.getSubmissionEndDate())
                .reviewStartDate(batch.getReviewStartDate())
                .reviewEndDate(batch.getReviewEndDate())
                .auctionStartTime(batch.getAuctionStartTime())
                .auctionEndTime(batch.getAuctionEndTime())
                .totalItemsSubmitted(batch.getTotalItemsSubmitted())
                .totalItemsApproved(batch.getTotalItemsApproved())
                .totalItemsRejected(batch.getTotalItemsRejected())
                .totalItemsSold(totalSold.intValue())
                .totalItemsUnsold(totalUnsold.intValue())
                .totalRevenue(batch.getTotalRevenue() != null ? batch.getTotalRevenue() : BigDecimal.ZERO)
                .averageSalePrice(averagePrice)
                .totalBids(totalBids)
                .uniqueBidders(uniqueBidders)
                .averageBidsPerItem(avgBidsPerItem != null ? avgBidsPerItem : 0.0)
                .approvalRate(approvalRate)
                .soldRate(soldRate)
                .conversionRate(conversionRate)
                .build();
    }

    public List<CategoryAnalytics> getCategoryAnalytics() {
        AuctionBatch currentBatch = batchRepository.findCurrentBatch()
                .orElseThrow(() -> new ResourceNotFoundException("No active batch found"));

        List<Object[]> results = itemRepository.getCategoryAnalytics(currentBatch.getId());
        BigDecimal totalRevenue = currentBatch.getTotalRevenue() != null ?
                currentBatch.getTotalRevenue() : BigDecimal.ZERO;

        List<CategoryAnalytics> analytics = new ArrayList<>();

        for (Object[] row : results) {
            String category = (String) row[0];
            Long totalItems = (Long) row[1];
            Long itemsSold = ((Number) row[2]).longValue();
            BigDecimal categoryRevenue = (BigDecimal) row[3];
            BigDecimal avgPrice = (BigDecimal) row[4];
            BigDecimal highestPrice = (BigDecimal) row[5];

            Long itemsUnsold = totalItems - itemsSold;
            Double soldRate = totalItems > 0 ? (itemsSold.doubleValue() / totalItems.doubleValue()) * 100 : 0.0;
            Double percentageOfTotal = totalRevenue.compareTo(BigDecimal.ZERO) > 0 ?
                    categoryRevenue.divide(totalRevenue, 4, RoundingMode.HALF_UP)
                            .multiply(BigDecimal.valueOf(100)).doubleValue() : 0.0;

            analytics.add(CategoryAnalytics.builder()
                    .category(category)
                    .totalItems(totalItems)
                    .itemsSold(itemsSold)
                    .itemsUnsold(itemsUnsold)
                    .totalRevenue(categoryRevenue)
                    .averagePrice(avgPrice)
                    .highestPrice(highestPrice)
                    .soldRate(soldRate)
                    .percentageOfTotalRevenue(percentageOfTotal)
                    .build());
        }

        return analytics;
    }

    public List<TopSeller> getTopSellers(int limit) {
        List<Object[]> results = itemRepository.getSellerStatistics(limit);

        List<TopSeller> topSellers = new ArrayList<>();
        int rank = 1;

        for (Object[] row : results) {
            Long sellerId = (Long) row[0];
            Long totalItems = (Long) row[1];
            Long totalSold = ((Number) row[2]).longValue();
            BigDecimal totalRevenue = (BigDecimal) row[3];

            User seller = userRepository.findById(sellerId).orElse(null);
            if (seller == null) continue;

            BigDecimal avgPrice = totalSold > 0 ?
                    totalRevenue.divide(BigDecimal.valueOf(totalSold), 2, RoundingMode.HALF_UP) : BigDecimal.ZERO;
            Double soldRate = totalItems > 0 ? (totalSold.doubleValue() / totalItems.doubleValue()) * 100 : 0.0;

            topSellers.add(TopSeller.builder()
                    .sellerId(sellerId)
                    .sellerName(seller.getFullName())
                    .sellerEmail(seller.getEmail())
                    .totalItemsSubmitted(totalItems)
                    .totalItemsSold(totalSold)
                    .totalRevenue(totalRevenue)
                    .averageSalePrice(avgPrice)
                    .soldRate(soldRate)
                    .rank(rank++)
                    .build());
        }
        return topSellers;
    }


    public List<TopBuyer> getTopBuyers(int limit) {
        List<Object[]> results = bidRepository.getBuyerStatistics(limit);

        List<TopBuyer> topBuyers = new ArrayList<>();
        int rank = 1;

        for (Object[] row : results) {
            Long buyerId = (Long) row[0];
            Long itemsWon = (Long) row[1];
            BigDecimal totalSpent = (BigDecimal) row[2];
            BigDecimal avgPrice = (BigDecimal) row[3];

            User buyer = userRepository.findById(buyerId).orElse(null);
            if (buyer == null) continue;

            Long totalBids = bidRepository.countByBidderId(buyerId);
            Double winRate = totalBids > 0 ? (itemsWon.doubleValue() / totalBids.doubleValue()) * 100 : 0.0;

            topBuyers.add(TopBuyer.builder()
                    .buyerId(buyerId)
                    .buyerName(buyer.getFullName())
                    .buyerEmail(buyer.getEmail())
                    .totalItemsWon(itemsWon)
                    .totalSpent(totalSpent)
                    .averagePurchasePrice(avgPrice)
                    .totalBidsPlaced(totalBids)
                    .winRate(winRate)
                    .rank(rank++)
                    .build());
        }

        return topBuyers;
    }

    public List<RevenueTimeSeries> getRevenueTimeSeries(int batchCount) {
        List<AuctionBatch> batches = batchRepository.findRecentBatches(batchCount);

        return batches.stream()
                .map(batch -> {
                    Long sold = itemRepository.countByBatchIdAndStatus(batch.getId(), "SOLD");
                    Long bids = bidRepository.countBidsByBatchId(batch.getId());

                    return RevenueTimeSeries.builder()
                            .date(batch.getAuctionEndTime() != null ?
                                    batch.getAuctionEndTime().toLocalDate() :
                                    batch.getSubmissionStartDate().toLocalDate())
                            .batchCode(batch.getBatchCode())
                            .revenue(batch.getTotalRevenue() != null ? batch.getTotalRevenue() : BigDecimal.ZERO)
                            .itemsSold(sold.intValue())
                            .totalBids(bids)
                            .build();
                })
                .collect(Collectors.toList());

    }
}
