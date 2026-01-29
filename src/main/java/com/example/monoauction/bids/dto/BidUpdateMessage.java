package com.example.monoauction.bids.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class BidUpdateMessage {
    private Long itemId;
    private Long bidId;
    private String bidderName;
    private BigDecimal amount;
    private Integer totalBids;
    private LocalDateTime bidTime;
    private String status;
}
