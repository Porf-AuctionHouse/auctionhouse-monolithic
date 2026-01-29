package com.example.monoauction.batch.service;

import com.example.monoauction.batch.model.AuctionBatch;
import com.example.monoauction.batch.repository.AuctionBatchRepository;
import com.example.monoauction.bids.model.Bid;
import com.example.monoauction.bids.repository.BidRepository;
import com.example.monoauction.common.enums.*;
import com.example.monoauction.item.model.AuctionItem;
import com.example.monoauction.item.repository.AuctionItemRepository;
import com.example.monoauction.notifications.service.WebSocketNotificationService;
import com.example.monoauction.payments.model.Transaction;
import com.example.monoauction.payments.repository.TransactionRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import javax.swing.*;
import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.TemporalAdjusters;
import java.time.temporal.WeekFields;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class AuctionBatchService {
    private final AuctionBatchRepository batchRepository;
    private final AuctionItemRepository itemRepository;
    private final BidRepository bidRepository;
    private final TransactionRepository transactionRepository;
    private final WebSocketNotificationService webSocketService;

    @Value("${auction.lifecycle.scheduler.enabled}")
    private boolean schedulerEnabled;

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

    public AuctionBatch createTestBatch(){
        if(schedulerEnabled){
            throw new RuntimeException("Scheduler is enabled");
        }

        LocalDateTime now = LocalDateTime.now();
        AuctionBatch batch = new AuctionBatch();
        batch.setBatchCode("TEST-BATCH-" + System.currentTimeMillis());
        batch.setWeekNumber(now.get(WeekFields.of(DayOfWeek.MONDAY, 1).weekOfYear()));
        batch.setYear(now.getYear());
        batch.setStatus(BatchStatus.SUBMISSION);

        batch.setSubmissionStartDate(now.minusMinutes(10));
        batch.setSubmissionEndDate(now.plusMinutes(5));

        batch.setReviewStartDate(now.plusMinutes(6));
        batch.setReviewEndDate(now.plusMinutes(10));

        batch.setAuctionStartTime(now.plusMinutes(11));
        batch.setAuctionEndTime(now.plusMinutes(15));

        batch.setTotalItemsSubmitted(0);
        batch.setTotalItemsApproved(0);
        batch.setTotalItemsRejected(0);
        batch.setTotalItemsSold(0);
        batch.setTotalRevenue(BigDecimal.ZERO);

        return batchRepository.save(batch);
    }

    public void batchLifecycle(Long batchId, BatchStatus status){
        if(schedulerEnabled){
            throw new RuntimeException("Scheduler is enabled");
        }

        AuctionBatch currentBatch = getBatchById(batchId);

        if(currentBatch.getStatus() == BatchStatus.SUBMISSION && status == BatchStatus.REVIEW){
                log.info("Transitioning To REVIEW Phase");

                transitionToReview(currentBatch);
        }

        else if(currentBatch.getStatus() == BatchStatus.REVIEW && status == BatchStatus.LIVE){
                log.info("Starting Auction");

                startAuction(currentBatch);
            }

            else if(currentBatch.getStatus() == BatchStatus.LIVE && status == BatchStatus.ENDED){
                log.info("Ending Auction");

                endAuction(currentBatch);
            }
    }


    private void transitionToReview(AuctionBatch batch){
        batch.setStatus(BatchStatus.REVIEW);
        batchRepository.save(batch);

        List<AuctionItem> submittedItems = itemRepository.findByBatchIdAndStatus(batch.getId(), ItemStatus.SUBMITTED);


        for(AuctionItem item : submittedItems){
            ItemStatus oldStatus = item.getStatus();
            item.setStatus(ItemStatus.UNDER_REVIEW);
            itemRepository.save(item);

            webSocketService.sendItemStatusUpdate(item, oldStatus);
        }

        webSocketService.sendAuctionStatusUpdate(batch,
                "Submission phase ended. Items are now under review.");

        log.info("Batch {} Transition To REVIEW, {} Items User Review.",
                batch.getBatchCode(), submittedItems.size());
    }

    private void startAuction(AuctionBatch batch){
        batch.setStatus(BatchStatus.LIVE);
        batchRepository.save(batch);

        List<AuctionItem> approvedItems = itemRepository
                .findByBatchIdAndStatus(batch.getId(), ItemStatus.APPROVED);

        for(AuctionItem item : approvedItems) {
            ItemStatus oldStatus = item.getStatus();
            item.setStatus(ItemStatus.LIVE);
            item.setAuctionStartedAt(LocalDateTime.now());
            itemRepository.save(item);

            webSocketService.sendItemStatusUpdate(item, oldStatus);
        }

        webSocketService.sendAuctionStatusUpdate(batch,
                "AUCTION IS NOW LIVE! Start Bidding!");

        log.info("Auction Started For {}. {} Item Now LIVE.", batch.getBatchCode(), approvedItems.size());
    }

    private void endAuction(AuctionBatch batch){
        batch.setStatus(BatchStatus.ENDED);
        batchRepository.save(batch);

        List<AuctionItem> liveItems = itemRepository
                .findByBatchIdAndStatus(batch.getId(), ItemStatus.LIVE);

        int soldCount = 0;
        int unsoldCount = 0;
        BigDecimal totalAmount = BigDecimal.ZERO;

        for(AuctionItem item : liveItems){
            boolean isSold = processItemEnd(item);

            if(isSold){
                soldCount++;
                totalAmount = totalAmount.add(item.getCurrentBid());
            }
            else{
                unsoldCount++;
            }
        }

        batch.setTotalItemsSold(soldCount);
        batch.setTotalRevenue(totalAmount);

        webSocketService.sendAuctionStatusUpdate(batch,
                "AUCTION ENDED! " + soldCount + " Items Sold.");

        log.info("Auction Ended For Batch {}. Items Sold: {} Items Unsold: {}, Total Revenue: {}",
                batch.getBatchCode(), soldCount, unsoldCount, totalAmount);


    }

    private boolean processItemEnd(AuctionItem item){
        ItemStatus oldStatus = item.getStatus();
        item.setAuctionEndedAt(LocalDateTime.now());

        Optional<Bid> winningBidOpt = bidRepository
                .findTopByItemIdOrderByAmountDesc(item.getId());

        if(winningBidOpt.isEmpty()){
            item.setStatus(ItemStatus.UNSOLD);
            itemRepository.save(item);

            webSocketService.sendItemStatusUpdate(item, oldStatus);

            log.info("Item {} - No Bid Received", item.getId());
            return false;
        }

        Bid winningBid = winningBidOpt.get();

        if(item.getReservePrice() != null &&
                winningBid.getAmount().compareTo(item.getReservePrice()) < 0){
            item.setStatus(ItemStatus.UNSOLD);
            winningBid.setStatus(BidStatus.LOST);

            itemRepository.save(item);
            bidRepository.save(winningBid);

            webSocketService.sendItemStatusUpdate(item, oldStatus);

            log.info("Item {} - Reserve Price Not Met. Highest Bid: {}, Reserve: {}",
                    item.getId(), winningBid.getAmount(), item.getReservePrice());
            return false;
        }

        item.setStatus(ItemStatus.SOLD);
        item.setWinnerId(winningBid.getBidderId());
        item.setSoldAt(LocalDateTime.now());

        winningBid.setStatus(BidStatus.WON);

        itemRepository.save(item);
        bidRepository.save(winningBid);

        webSocketService.sendItemStatusUpdate(item, oldStatus);

        List<Bid> otherBids = bidRepository.findByItemIdAndStatusNot(item.getId(), BidStatus.WON);

        otherBids.forEach(otherBid -> {
            otherBid.setStatus(BidStatus.LOST);
            bidRepository.save(otherBid);
        });

        createTransaction(item, winningBid);

        log.info("Item {} - Sold To User {} For {}"
                , item.getId(), winningBid.getBidderId(), winningBid.getAmount());
        return true;
    }

    private void createTransaction(AuctionItem item, Bid winningBid){
        Transaction transaction = new Transaction();

        transaction.setAuctionItemId(item.getId());
        transaction.setBuyerId(winningBid.getBidderId());
        transaction.setSellerId(item.getSellerId());
        transaction.setWinningBidId(winningBid.getId());
        transaction.setAmount(winningBid.getAmount());

        BigDecimal platformFee = winningBid.getAmount().multiply(new BigDecimal("0.05"));
        transaction.setPlatformFee(platformFee);

        BigDecimal sellerPayout = winningBid.getAmount().subtract(platformFee);
        transaction.setSellerPayout(sellerPayout);

        transaction.setStatus(TransactionStatus.PENDING);
        transaction.setPaymentMethod(PaymentMethod.PENDING);

        transactionRepository.save(transaction);

        log.info("Transaction Created For Item {}, Amount: {}, Fee: {}, Payout {}."
                , item.getId(), winningBid.getAmount(), platformFee, sellerPayout);
    }

}
