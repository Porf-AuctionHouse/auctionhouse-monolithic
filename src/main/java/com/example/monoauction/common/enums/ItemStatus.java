package com.example.monoauction.common.enums;

public enum ItemStatus {
    SUBMITTED,         // Seller just submitted
    UNDER_REVIEW,      // Admin is reviewing
    CHANGES_REQUESTED, // Admin wants changes
    APPROVED,          // Ready for auction
    REJECTED,          // Not accepted
    LIVE,              // Currently in auction
    SOLD,              // Has winner
    UNSOLD             // No bids or reserve not met
}
