package com.example.monoauction.notifications.event;

import com.example.monoauction.item.model.AuctionItem;
import com.example.monoauction.user.model.User;
import lombok.Getter;
import org.springframework.context.ApplicationEvent;

import java.util.List;

@Getter
public class WatchlistItemGoingLiveEvent extends ApplicationEvent {
    private final AuctionItem item;
    private final List<User> watchers;

    public WatchlistItemGoingLiveEvent(Object source, AuctionItem item, List<User> watchers) {
        super(source);
        this.item = item;
        this.watchers = watchers;
    }
}