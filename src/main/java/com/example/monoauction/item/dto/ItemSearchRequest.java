package com.example.monoauction.item.dto;

import com.example.monoauction.common.enums.ItemCategory;
import com.example.monoauction.common.enums.ItemStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ItemSearchRequest {

    private String keyword;

    private ItemCategory category;
    private ItemStatus status;
    private BigDecimal minPrice;
    private BigDecimal maxPrice;
    private Long sellerId;
    private Long batchId;

    private String sortBy;
    private String sortOrder;

    private Integer page = 0;
    private Integer size = 20;

    private Boolean hasReservePrice;
    private Boolean hasBids;
    private Integer minBids;

}
