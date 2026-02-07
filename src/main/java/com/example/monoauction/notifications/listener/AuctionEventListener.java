package com.example.monoauction.notifications.listener;

import com.example.monoauction.batch.model.AuctionBatch;
import com.example.monoauction.batch.repository.AuctionBatchRepository;
import com.example.monoauction.bids.model.Bid;
import com.example.monoauction.bids.repository.BidRepository;
import com.example.monoauction.common.execptions.ResourceNotFoundException;
import com.example.monoauction.item.model.AuctionItem;
import com.example.monoauction.item.repository.AuctionItemRepository;
import com.example.monoauction.notifications.event.*;
import com.example.monoauction.notifications.service.EmailService;
import com.example.monoauction.user.model.User;
import com.example.monoauction.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@Slf4j
@RequiredArgsConstructor
public class AuctionEventListener {
    private final EmailService emailService;
    private final UserRepository userRepository;
    private final AuctionBatchRepository batchRepository;
    private final AuctionItemRepository itemRepository;
    private final BidRepository bidRepository;

    @EventListener
    @Async
    public void handleItemSubmittedEvent(ItemSubmittedEvent event){
        try{
            AuctionItem item = event.getItem();
            User seller = userRepository.findById(item.getSellerId())
                    .orElseThrow(() -> new ResourceNotFoundException("Seller not found"));
            emailService.sendItemSubmittedEmail(seller, item);
            log.info("Item submitted email sent to {}", seller.getEmail());
        }catch(Exception e){
            log.error("Error sending item submitted email", e);
        }
    }

    @EventListener
    @Async
    public void handleItemApproved(ItemApprovedEvent event){
        try {
            AuctionItem item = event.getItem();
            User seller = userRepository.findById(item.getSellerId())
                    .orElseThrow(() -> new ResourceNotFoundException("Seller not found"));
            AuctionBatch batch = batchRepository.findById(item.getBatchId())
                            .orElseThrow(() -> new ResourceNotFoundException("Batch not found"));
            emailService.sendItemApprovedEmail(seller, item, batch);
            log.info("Item approved email sent to {}", seller.getEmail());

        } catch (Exception e){
            log.error("Error sending item approved email", e);
        }
    }

    @EventListener
    @Async
    public void handleItemRejected(ItemRejectedEvent event) {
        AuctionItem item = event.getItem();
        User seller = userRepository.findById(item.getSellerId())
                .orElseThrow(() -> new ResourceNotFoundException("Seller not found"));
        emailService.sendItemRejectedEmail(seller, item, event.getReason());
        log.info("Item rejected email sent to {}", seller.getEmail());
    }

    @EventListener
    @Async
    public void handleBidPlaced(BidPlacedEvent event){
        try{
            Bid bid = event.getBid();
            AuctionItem item = event.getItem();
            User seller = userRepository.findById(item.getSellerId())
                    .orElseThrow(() -> new ResourceNotFoundException("Seller not found"));
            emailService.sendNewBidEmail(seller, item, bid);
            log.info("New bid email sent to {}", seller.getEmail());
        } catch(Exception e){
            log.error("Error sending bid placed email", e);
        }
    }

    @EventListener
    @Async
    public void handleOutBid(OutbidEvent event){
        try{
            Bid bid = event.getBid();
            User bidder = userRepository.findById(bid.getBidderId())
                            .orElseThrow(() -> new ResourceNotFoundException("Bidder not found"));
            emailService.sendOutbidEmail(bidder, event.getItem(), event.getNewAmount());
            log.info("Outbid email sent to {}", bidder.getEmail());
        }catch(Exception e){
            log.error("Error sending outbid email", e);
        }
    }

    @EventListener
    @Async
    public void handleAuctionStarted(AuctionStartedEvent event) {
        try {
            AuctionBatch batch = event.getBatch();

            List<User> activeUsers = userRepository.findByIsActiveTrue();

            for(User user : activeUsers){
                emailService.sendAuctionStartedEmail(user, batch);
            }

            log.info("Auction started email sent to {} users", activeUsers.size());

        } catch (Exception e) {
            log.error("Error sending auction started email", e);
        }
    }

    @EventListener
    @Async
    public void handleWatchlistItemBid(WatchlistItemBidEvent event) {
        try {
            AuctionItem item = event.getItem();
            Bid bid = event.getBid();
            List<User> watchers = event.getWatchers();

            // Get the bidder to exclude them from notifications
            User bidder = userRepository.findById(bid.getBidderId())
                    .orElse(null);

            for (User watcher : watchers) {
                // Don't notify the bidder about their own bid
                if (bidder != null && watcher.getId().equals(bidder.getId())) {
                    continue;
                }

                emailService.sendWatchlistItemBidEmail(watcher, item, bid);
            }

            log.info("Watchlist bid notifications sent to {} watchers for item {}",
                    watchers.size(), item.getId());

        } catch (Exception e) {
            log.error("Error sending watchlist bid notifications", e);
        }
    }

    @EventListener
    @Async
    public void handleWatchlistItemGoingLive(WatchlistItemGoingLiveEvent event) {
        try {
            AuctionItem item = event.getItem();
            List<User> watchers = event.getWatchers();

            for (User watcher : watchers) {
                emailService.sendWatchlistItemGoingLiveEmail(watcher, item);
            }

            log.info("Watchlist going live notifications sent to {} watchers for item {}",
                    watchers.size(), item.getId());

        } catch (Exception e) {
            log.error("Error sending watchlist going live notifications", e);
        }
    }

    @EventListener
    @Async
    public void handleWatchlistItemEndingSoon(WatchlistItemEndingSoonEvent event) {
        try {
            AuctionItem item = event.getItem();
            List<User> watchers = event.getWatchers();
            int hoursRemaining = event.getHoursRemaining();

            for (User watcher : watchers) {
                emailService.sendWatchlistItemEndingSoonEmail(watcher, item, hoursRemaining);
            }

            log.info("Watchlist ending soon notifications sent to {} watchers for item {}",
                    watchers.size(), item.getId());

        } catch (Exception e) {
            log.error("Error sending watchlist ending soon notifications", e);
        }
    }
}
