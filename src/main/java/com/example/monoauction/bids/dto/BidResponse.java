package com.example.monoauction.bids.dto;

import com.example.monoauction.bids.model.Bid;
import com.example.monoauction.common.enums.BidStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BidResponse {
    private Long id;
    private Long itemId;
    private Long bidderId;
    private String bidderName;
    private BigDecimal amount;
    private BidStatus status;
    private LocalDateTime bidTime;

    public BidResponse(Bid bid){
        this.id = bid.getId();
        this.itemId = bid.getItemId();
        this.bidderId = bid.getBidderId();
        this.bidderName = bid.getBidderName();
        this.amount = bid.getAmount();
        this.status = bid.getStatus();
        this.bidTime = bid.getBidTime();
    }
}
