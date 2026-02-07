package com.example.monoauction.watchlist.service;

import com.example.monoauction.common.execptions.BusinessException;
import com.example.monoauction.common.execptions.ResourceNotFoundException;
import com.example.monoauction.item.model.AuctionItem;
import com.example.monoauction.item.repository.AuctionItemRepository;
import com.example.monoauction.user.model.User;
import com.example.monoauction.user.repository.UserRepository;
import com.example.monoauction.watchlist.dto.WatchlistRequest;
import com.example.monoauction.watchlist.dto.WatchlistResponse;
import com.example.monoauction.watchlist.model.WatchlistItem;
import com.example.monoauction.watchlist.repository.WatchlistRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class WatchlistService {

    private final WatchlistRepository watchlistRepository;
    private final UserRepository userRepository;
    private final AuctionItemRepository auctionItemRepository;

    @Transactional
    public WatchlistResponse addToWatchlist(Long userId, WatchlistRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User Not Found"));

        AuctionItem item = auctionItemRepository.findById(request.getItemId())
                .orElseThrow(() -> new ResourceNotFoundException("Item Not Found"));

        if (watchlistRepository.existsByUserIdAndAuctionItemId(userId, request.getItemId())) {
            throw new BusinessException("Item already exists in watchlist");
        }

        if (item.getSellerId().equals(userId)) {
            throw new BusinessException("You cannot add your own items to watchlist");
        }

        WatchlistItem watchlistItem = WatchlistItem.builder()
                .user(user)
                .auctionItem(item)
                .notifyOnBid(request.getNotifyOnBid())
                .notifyOnPriceDrop(request.getNotifyOnPriceDrop())
                .build();

        WatchlistItem savedWatchlistItem = watchlistRepository.save(watchlistItem);
        log.info("User {} added item {} to watchlist", userId, request.getItemId());

        return convertToWatchlistResponse(savedWatchlistItem);
    }

    @Transactional
    public void removeFromWatchlist(Long userId, Long itemId) {

        if (!watchlistRepository.existsByUserIdAndAuctionItemId(userId, itemId)) {
            throw new BusinessException("Item not found in your watchlist");
        }

        watchlistRepository.deleteByUserIdAndAuctionItemId(userId, itemId);
        log.info("User {} removed item {} from watchlist", userId, itemId);
    }

    @Transactional(readOnly = true)
    public List<WatchlistResponse> getUserWatchlist(Long userId) {
        List<WatchlistItem> watchlistItems = watchlistRepository.findByUserId(userId);
        return watchlistItems.stream()
                .map(this::convertToWatchlistResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public List<WatchlistResponse> getLiveWatchlistItems(Long userId) {
        List<WatchlistItem> watchlistItems = watchlistRepository.findByUserIdAndItemStatus(userId, "LIVE");
        return watchlistItems.stream()
                .map(this::convertToWatchlistResponse)
                .collect(Collectors.toList());
    }

    public boolean isInWatchlist(Long userId, Long itemId) {
        return watchlistRepository.existsByUserIdAndAuctionItemId(userId, itemId);
    }

    @Transactional
    public WatchlistResponse updateNotificationPreferences(
            Long userId,
            Long itemId,
            Boolean notifyOnBid,
            Boolean notifyOnPriceDrop
    ) {
        WatchlistItem watchlistItem = watchlistRepository
                .findByUserIdAndAuctionItemId(userId, itemId)
                .orElseThrow(() -> new ResourceNotFoundException("Item not found in your watchlist"));

        if (notifyOnBid != null) {
            watchlistItem.setNotifyOnBid(notifyOnBid);
        }

        if (notifyOnPriceDrop != null) {
            watchlistItem.setNotifyOnPriceDrop(notifyOnPriceDrop);
        }

        WatchlistItem updated = watchlistRepository.save(watchlistItem);
        return convertToWatchlistResponse(updated);

    }

    public long getWatchlistCount(Long userId) {
        return watchlistRepository.countByUserId(userId);
    }

    private WatchlistResponse convertToWatchlistResponse(WatchlistItem watchlistItem) {
        AuctionItem item = auctionItemRepository.findById(watchlistItem.getAuctionItem().getId())
                .orElseThrow(() -> new ResourceNotFoundException("Item not found"));

        User seller = userRepository.findById(item.getSellerId())
                .orElseThrow(() -> new ResourceNotFoundException("Seller not found"));

        return WatchlistResponse.builder()
                .watchlistId(watchlistItem.getId())
                .itemId(item.getId())
                .itemTitle(item.getTitle())
                .itemDescription(item.getDescription())
                .itemCategory(item.getCategory().name())
                .currentBid(item.getCurrentBid())
                .reservePrice(item.getReservePrice())
                .itemStatus(item.getStatus().name())
                .sellerName(seller.getFullName())
                .totalBids(item.getTotalBids())
                .addedToWatchlistAt(watchlistItem.getAddedAt())
                .notifyOnBid(watchlistItem.getNotifyOnBid())
                .notifyOnPriceDrop(watchlistItem.getNotifyOnPriceDrop())
                .imageUrl(item.getImageUrls())
                .build();

    }
}
