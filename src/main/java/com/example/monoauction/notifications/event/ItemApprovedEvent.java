package com.example.monoauction.notifications.event;

import com.example.monoauction.item.model.AuctionItem;

public class ItemApprovedEvent {
    private final AuctionItem item;

    public ItemApprovedEvent(AuctionItem item){
        this.item = item;
    }

    public AuctionItem getItem(){
        return item;
    }
}
