package com.example.monoauction.notifications.event;

import com.example.monoauction.item.model.AuctionItem;
import com.example.monoauction.user.model.User;
import lombok.Getter;
import org.springframework.context.ApplicationEvent;

import java.util.List;

@Getter
public class WatchlistItemEndingSoonEvent extends ApplicationEvent {
    private final AuctionItem item;
    private final List<User> watchers;
    private final int hoursRemaining;

    public WatchlistItemEndingSoonEvent(Object source, AuctionItem item, List<User> watchers, int hoursRemaining) {
        super(source);
        this.item = item;
        this.watchers = watchers;
        this.hoursRemaining = hoursRemaining;
    }
}