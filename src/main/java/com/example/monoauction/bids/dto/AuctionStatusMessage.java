package com.example.monoauction.bids.dto;

import com.example.monoauction.common.enums.BatchStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AuctionStatusMessage {
    private String batchCode;
    private BatchStatus status;
    private LocalDateTime timestamp;
    private String message;
    private Long minutesRemaining;
}