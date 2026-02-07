package com.example.monoauction.watchlist.controller;

import com.example.monoauction.common.dto.ApiResponse;
import com.example.monoauction.security.SecurityUtils;
import com.example.monoauction.watchlist.dto.WatchlistRequest;
import com.example.monoauction.watchlist.dto.WatchlistResponse;
import com.example.monoauction.watchlist.service.WatchlistService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/watchlist")
@RequiredArgsConstructor
public class WatchlistController {

    private final WatchlistService watchlistService;

    @PostMapping()
    public ResponseEntity<ApiResponse<WatchlistResponse>> addToWatchlist(@RequestBody WatchlistRequest request) {
        Long userId = SecurityUtils.getCurrentUserId();

        WatchlistResponse response = watchlistService.addToWatchlist(userId, request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Item added to watchlist",response));
    }

    @DeleteMapping("/items/{itemId}")
    public ResponseEntity<ApiResponse<Void>> removeFromWatchlist(
            @PathVariable Long itemId) {

        Long userId = SecurityUtils.getCurrentUserId();
        watchlistService.removeFromWatchlist(userId, itemId);
        return ResponseEntity.ok(ApiResponse.success("Item removed from watchlist", null));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<WatchlistResponse>>> getWatchlist() {
        Long userId = SecurityUtils.getCurrentUserId();
        List<WatchlistResponse> watchlist = watchlistService.getUserWatchlist(userId);
        return ResponseEntity.ok(ApiResponse.success(watchlist));
    }

    @GetMapping("/live")
    public ResponseEntity<ApiResponse<List<WatchlistResponse>>> getLiveWatchlistItems() {
        Long userId = SecurityUtils.getCurrentUserId();
        List<WatchlistResponse> liveItems = watchlistService.getLiveWatchlistItems(userId);
        return ResponseEntity.ok(ApiResponse.success(liveItems));
    }

    @GetMapping("/check/{itemId}")
    public ResponseEntity<ApiResponse<Map<String, Boolean>>> checkIfInWatchlist(
            @PathVariable Long itemId) {

        Long userId = SecurityUtils.getCurrentUserId();
        boolean isInWatchlist = watchlistService.isInWatchlist(userId, itemId);
        return ResponseEntity.ok(ApiResponse.success(Map.of("isInWatchlist", isInWatchlist)));
    }

    @PatchMapping("/items/{itemId}/notifications")
    public ResponseEntity<ApiResponse<WatchlistResponse>> updateNotifications(
            @PathVariable Long itemId,
            @RequestBody Map<String, Boolean> preferences) {

        Long userId = SecurityUtils.getCurrentUserId();
        WatchlistResponse updated = watchlistService.updateNotificationPreferences(
                userId,
                itemId,
                preferences.get("notifyOnBid"),
                preferences.get("notifyOnPriceDrop")
        );
        return ResponseEntity.ok(ApiResponse.success(updated));
    }

    @GetMapping("/count")
    public ResponseEntity<ApiResponse<Map<String, Long>>> getWatchlistCount() {

        Long userId = SecurityUtils.getCurrentUserId();
        long count = watchlistService.getWatchlistCount(userId);
        return ResponseEntity.ok(ApiResponse.success(Map.of("count", count)));
    }


}
