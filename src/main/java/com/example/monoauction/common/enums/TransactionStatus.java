package com.example.monoauction.common.enums;

public enum TransactionStatus {
    PENDING,     // Created but not paid
    COMPLETED,   // Payment successful
    FAILED,      // Payment failed
    REFUNDED     // Money returned
}
