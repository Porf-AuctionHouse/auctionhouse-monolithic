package com.example.monoauction.watchlist.dto;

import lombok.Data;

import jakarta.validation.constraints.NotNull;

@Data
public class WatchlistRequest {
    @NotNull(message = "ItemId can't be null")
    private Long itemId;
    private Boolean notifyOnBid = true;
    private Boolean notifyOnPriceDrop = false;
}
