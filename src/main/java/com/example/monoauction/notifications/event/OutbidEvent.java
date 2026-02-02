package com.example.monoauction.notifications.event;

import com.example.monoauction.bids.model.Bid;
import com.example.monoauction.item.model.AuctionItem;
import com.example.monoauction.user.model.User;

import java.math.BigDecimal;

public class OutbidEvent {
    private final Bid bid;
    private final AuctionItem item;
    private final BigDecimal newAmount;

    public OutbidEvent(Bid bid, AuctionItem item, BigDecimal newAmount){
        this.bid = bid;
        this.item = item;
        this.newAmount = newAmount;
    }

    public Bid getBid() {
        return bid;
    }
    public AuctionItem getItem() {
        return item;
    }

    public BigDecimal getNewAmount() {
        return newAmount;
    }
}
