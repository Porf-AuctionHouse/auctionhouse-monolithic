package com.example.monoauction.common.enums;

public enum BidStatus {
    ACTIVE,     // Current bid (not yet outbid)
    OUTBID,     // Someone bid higher
    WINNING,    // Highest bid (auction still running)
    WON,        // Auction ended, this bid won
    LOST        // Auction ended, this bid lost
}
