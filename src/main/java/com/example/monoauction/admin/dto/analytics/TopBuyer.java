package com.example.monoauction.admin.dto.analytics;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TopBuyer {
    private Long buyerId;
    private String buyerName;
    private String buyerEmail;
    private Long totalItemsWon;
    private BigDecimal totalSpent;
    private BigDecimal averagePurchasePrice;
    private Long totalBidsPlaced;
    private Double winRate; // Won / Bids Placed
    private Integer rank;
}
