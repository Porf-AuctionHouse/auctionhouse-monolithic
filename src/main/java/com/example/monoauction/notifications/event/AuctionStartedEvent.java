package com.example.monoauction.notifications.event;

import com.example.monoauction.batch.model.AuctionBatch;

public class AuctionStartedEvent {
    private final AuctionBatch batch;

    public AuctionStartedEvent(AuctionBatch batch) {
        this.batch = batch;
    }

    public AuctionBatch getBatch() {
        return batch;
    }
}
