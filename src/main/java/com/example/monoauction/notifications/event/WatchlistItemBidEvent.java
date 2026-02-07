package com.example.monoauction.notifications.event;

import com.example.monoauction.bids.model.Bid;
import com.example.monoauction.item.model.AuctionItem;
import com.example.monoauction.user.model.User;
import lombok.Getter;
import org.springframework.context.ApplicationEvent;

import java.util.List;

@Getter
public class WatchlistItemBidEvent extends ApplicationEvent {
    private final AuctionItem item;
    private final Bid bid;
    private final List<User> watchers;

    public WatchlistItemBidEvent(Object source, AuctionItem item, Bid bid, List<User> watchers) {
        super(source);
        this.item = item;
        this.bid = bid;
        this.watchers = watchers;
    }
}