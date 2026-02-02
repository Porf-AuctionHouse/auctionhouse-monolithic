package com.example.monoauction.notifications.event;

import com.example.monoauction.item.model.AuctionItem;

public class ItemRejectedEvent {
    private AuctionItem item;
    private final String reason;

    public ItemRejectedEvent(AuctionItem item, String reason){
        this.item = item;
        this.reason = reason;
    }

    public AuctionItem getItem() {
        return item;
    }

    public String getReason() {
        return reason;
    }
}
