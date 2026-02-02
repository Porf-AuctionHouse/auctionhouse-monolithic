package com.example.monoauction.notifications.event;

import com.example.monoauction.item.model.AuctionItem;

public class ItemSubmittedEvent {
    private final AuctionItem item;

    public ItemSubmittedEvent(AuctionItem item) {
        this.item = item;
    }

    public AuctionItem getItem() {
        return item;
    }
}
