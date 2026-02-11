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
public class CategoryAnalytics {
    private String category;
    private Long totalItems;
    private Long itemsSold;
    private Long itemsUnsold;
    private BigDecimal totalRevenue;
    private BigDecimal averagePrice;
    private BigDecimal highestPrice;
    private Long totalBids;
    private Double soldRate;
    private Double percentageOfTotalRevenue;
}
