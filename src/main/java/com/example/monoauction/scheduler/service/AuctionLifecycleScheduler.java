package com.example.monoauction.scheduler.service;

import com.example.monoauction.batch.model.AuctionBatch;
import com.example.monoauction.batch.service.AuctionBatchService;
import com.example.monoauction.bids.model.Bid;
import com.example.monoauction.bids.repository.BidRepository;
import com.example.monoauction.common.enums.*;
import com.example.monoauction.item.model.AuctionItem;
import com.example.monoauction.item.repository.AuctionItemRepository;
import com.example.monoauction.notifications.service.WebSocketNotificationService;
import com.example.monoauction.payments.model.Transaction;
import com.example.monoauction.payments.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.cache.spi.support.AbstractReadWriteAccess;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuctionLifecycleScheduler {

    private final AuctionBatchService batchService;
    private final AuctionItemRepository itemRepository;
    private final BidRepository bidRepository;
    private final TransactionRepository transactionRepository;
    private final WebSocketNotificationService webSocketService;

    @Value("${auction.lifecycle.scheduler.enabled}")
    private boolean schedulerEnabled;

    @Scheduled(fixedDelay = 60000)
    public void checkAndUpdateBatchStatus(){
        log.info("Checking Batch Status");
        if(!schedulerEnabled){
            log.info("Scheduler is disabled. Skipping batch status check.");
            return;
        }
        try{
            AuctionBatch currentBatch = batchService.getCurrentBatch();
            LocalDateTime now = LocalDateTime.now();

            log.info("Check Batch Status. Current: {}, Time: {}",
                    currentBatch.getStatus(), now);

            if(currentBatch.getStatus() == BatchStatus.SUBMISSION &&
                now.isAfter(currentBatch.getSubmissionEndDate())){
                log.info("Transitioning To REVIEW Phase");

                transitionToReview(currentBatch);
            }

            else if(currentBatch.getStatus() == BatchStatus.REVIEW &&
                    now.isAfter(currentBatch.getReviewEndDate()) &&
                    now.isAfter(currentBatch.getAuctionStartTime().minusMinutes(1))){
                log.info("Starting Auction");

                startAuction(currentBatch);
            }

            else if(currentBatch.getStatus() == BatchStatus.LIVE &&
                    now.isAfter(currentBatch.getAuctionEndTime())){
                log.info("Ending Auction");

                endAuction(currentBatch);
            }


        } catch (Exception e) {
            log.error("Error In Batch Status Check: {}", e.getMessage(), e);
        }
    }

    private void transitionToReview(AuctionBatch batch){

        List<AuctionItem> submittedItems = itemRepository.findByBatchIdAndStatus(batch.getId(), ItemStatus.SUBMITTED);


        for(AuctionItem item : submittedItems){
            ItemStatus oldStatus = item.getStatus();
            item.setStatus(ItemStatus.UNDER_REVIEW);
            itemRepository.save(item);

            webSocketService.sendItemStatusUpdate(item, oldStatus);
        }

        webSocketService.sendAuctionStatusUpdate(batch,
                "Submission phase ended. Items are now under review.");

        batchService.updateBatchStatus(batch.getId(), BatchStatus.REVIEW);

        log.info("Batch {} Transition To REVIEW, {} Items User Review.",
                batch.getBatchCode(), submittedItems.size());
    }

    private void startAuction(AuctionBatch batch){
        batch.setStatus(BatchStatus.LIVE);

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

    @Scheduled(cron = "0 0 19 * * SUN")
    public void notifyAuctionEndingSoon(){
        try{
            AuctionBatch currentBatch = batchService.getCurrentBatch();

            if(currentBatch.getStatus() == BatchStatus.LIVE){
                log.info("Auction Ending In 1 Hour! Batch: {}",
                        currentBatch.getBatchCode());

            }

        } catch (Exception e) {
            log.error("Error In Auction Ending Soon Notification: {}", e.getMessage(), e);
        }
    }

    @Scheduled(fixedDelay = 300000)
    public void healthCheck(){
        log.info("Scheduler Health Check - Running Normally");
    }

}
