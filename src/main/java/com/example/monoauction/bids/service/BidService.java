package com.example.monoauction.bids.service;

import com.example.monoauction.batch.service.AuctionBatchService;
import com.example.monoauction.bids.model.Bid;
import com.example.monoauction.bids.repository.BidRepository;
import com.example.monoauction.common.enums.BidStatus;
import com.example.monoauction.common.enums.ItemStatus;
import com.example.monoauction.item.model.AuctionItem;
import com.example.monoauction.item.repository.AuctionItemRepository;
import com.example.monoauction.notifications.event.BidPlacedEvent;
import com.example.monoauction.notifications.event.OutbidEvent;
import com.example.monoauction.notifications.event.WatchlistItemBidEvent;
import com.example.monoauction.notifications.service.WebSocketNotificationService;
import com.example.monoauction.user.model.User;
import com.example.monoauction.user.repository.UserRepository;
import com.example.monoauction.watchlist.model.WatchlistItem;
import com.example.monoauction.watchlist.repository.WatchlistRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class BidService {

    private final BidRepository bidRepository;
    private final AuctionItemRepository itemRepository;
    private final AuctionBatchService batchService ;
    private final UserRepository userRepository;
    private final WebSocketNotificationService webSocketService;
    private final ApplicationEventPublisher eventPublisher;
    private final WatchlistRepository watchlistRepository;

    public Bid placeBid(Long itemId, Long bidderId, BigDecimal bidAmount) {
        if(!batchService.isAuctionLive()){
            throw new RuntimeException("Auction Is Not Live");
        }

        AuctionItem item = itemRepository.findById(itemId)
                .orElseThrow(() -> new RuntimeException("Item Not Found With These Details"));
        User bidder = userRepository.findById(bidderId)
                .orElseThrow(() -> new RuntimeException("User Not Found With These Details"));

        validateBid(item, bidder, bidAmount);

        Optional<Bid> previousHighestBid = bidRepository.findTopByItemIdOrderByAmountDesc(itemId);

        previousHighestBid.ifPresent(prevBid -> {
            prevBid.setStatus(BidStatus.OUTBID);
            bidRepository.save(prevBid);

            webSocketService.sendOutbidNotification(
                    prevBid.getBidderId(),
                    itemId,
                    bidAmount
            );
            eventPublisher.publishEvent(new OutbidEvent(prevBid, item, bidAmount));
        });

        Bid bid = new Bid();
        bid.setItemId(itemId);
        bid.setBidderId(bidderId);
        bid.setAmount(bidAmount);
        bid.setStatus(BidStatus.WINNING);
        bid.setBidTime(LocalDateTime.now());
        bid.setBidderName(bidder.getFullName());

        Bid savedBid = bidRepository.save(bid);

        item.setCurrentBid(bidAmount);
        item.setTotalBids(item.getTotalBids() + 1);
        itemRepository.save(item);

        webSocketService.sendBidUpdate(itemId,savedBid);
        notifyWatchers(item, savedBid);
        eventPublisher.publishEvent(new BidPlacedEvent(savedBid, item));

        return savedBid;

    }

    public void validateBid(AuctionItem item, User bidder, BigDecimal bidAmount){
        if(item.getStatus() != ItemStatus.LIVE){
            throw new RuntimeException("Item Is Not Available For Bidding");
        }

        if(item.getSellerId().equals(bidder.getId())){
            throw new RuntimeException("You Cannot Bid On Your Own Items");
        }



        BigDecimal minimumBid = item.getCurrentBid() != null
                ? item.getCurrentBid().add(item.getBidIncrement())
                : item.getStartingPrice();

        if(bidAmount.compareTo(minimumBid) < 0){
            throw new RuntimeException("Bid must be at least "+minimumBid);
        }


    }

    public List<Bid> getBidHistory(Long itemId){
        return bidRepository.findByItemIdOrderByBidTimeDesc(itemId);
    }

    public List<Bid> getUserBid(Long userId){
        return bidRepository.findByBidderIdOrderByBidTimeDesc(userId);
    }

    public Optional<Bid> getHighestBid(Long itemId){
        return bidRepository.findTopByItemIdOrderByAmountDesc(itemId);
    }

    public BigDecimal calculatedMinimumBid(Long itemId){
        AuctionItem item = itemRepository.findById(itemId)
                .orElseThrow(() -> new RuntimeException("Item Not Found With These Details"));
        
        return item.getCurrentBid() != null
                ? item.getCurrentBid().add(item.getBidIncrement())
                : item.getStartingPrice();
    }

    private void notifyWatchers(AuctionItem item, Bid bid) {
        try {
            List<WatchlistItem> watchlistItems = watchlistRepository.findUsersWatchingItem(item.getId());

            List<User> watchers = watchlistItems.stream()
                    .filter(wi -> wi.getNotifyOnBid() != null && wi.getNotifyOnBid())
                    .map(wi -> userRepository.findById(wi.getUser().getId()).orElse(null))
                    .collect(Collectors.toList());

            if (!watchers.isEmpty()) {
                eventPublisher.publishEvent(new WatchlistItemBidEvent(this, item, bid, watchers));
            }

        } catch (Exception e) {
            log.error("Error notifying watchers for item {}", item.getId(), e);
        }
    }

}
