package com.example.monoauction.admin.dto.analytics;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RevenueTimeSeries {
    private LocalDate date;
    private String batchCode;
    private BigDecimal revenue;
    private Integer itemsSold;
    private Long totalBids;

}
