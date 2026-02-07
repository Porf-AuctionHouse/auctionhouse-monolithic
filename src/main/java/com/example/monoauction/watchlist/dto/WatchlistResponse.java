package com.example.monoauction.watchlist.dto;

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
public class WatchlistResponse {
    
    private Long watchlistId;
    private Long itemId;
    private String itemTitle;
    private String itemDescription;
    private String itemCategory;
    private BigDecimal currentBid;
    private BigDecimal reservePrice;
    private String itemStatus;
    private String sellerName;
    private int totalBids;
    private LocalDateTime auctionEndTime;
    private LocalDateTime addedToWatchlistAt;
    private Boolean notifyOnBid;
    private Boolean notifyOnPriceDrop;
    private String imageUrl;
}
