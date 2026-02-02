package com.example.monoauction.notifications.event;

import com.example.monoauction.bids.model.Bid;
import com.example.monoauction.item.model.AuctionItem;

public class BidPlacedEvent {
    private final Bid bid;
    private final AuctionItem item;

    public BidPlacedEvent(Bid bid, AuctionItem item) {
        this.bid = bid;
        this.item = item;
    }

    public Bid getBid() {
        return bid;
    }

    public AuctionItem getItem() {
        return item;
    }
}
