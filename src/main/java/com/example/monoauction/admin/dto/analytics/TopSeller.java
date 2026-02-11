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
public class TopSeller {
    private Long sellerId;
    private String sellerName;
    private String sellerEmail;
    private Long totalItemsSubmitted;
    private Long totalItemsSold;
    private BigDecimal totalRevenue;
    private BigDecimal averageSalePrice;
    private Double soldRate;
    private Integer rank;
}
