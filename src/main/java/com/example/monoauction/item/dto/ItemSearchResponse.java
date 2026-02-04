package com.example.monoauction.item.dto;

import com.example.monoauction.common.enums.ItemCategory;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ItemSearchResponse {

    private List<ItemResponse> items;

    private int currentPage;
    private int totalPages;
    private long totalItems;
    private int pageSize;
    private boolean hasNext;
    private boolean hasPrevious;

    private String searchKeyword;
    private Map<String, Object> additionalFilters;

    private List<ItemCategory> availableCategories;
    private Map<ItemCategory, Long> categoryCount;

    private BigDecimal minPrice;
    private BigDecimal maxPrice;

}
