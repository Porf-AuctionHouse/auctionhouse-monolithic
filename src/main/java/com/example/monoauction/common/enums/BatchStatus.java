package com.example.monoauction.common.enums;

public enum BatchStatus {
    SUBMISSION,    // Monday-Wednesday
    REVIEW,        // Thursday-Friday
    LIVE,          // Saturday-Sunday
    ENDED,         // After Sunday 8 PM
    SETTLED        // Payments processed
}
