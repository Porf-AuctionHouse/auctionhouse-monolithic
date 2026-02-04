package com.example.monoauction.item.controller;

import com.example.monoauction.common.dto.ApiResponse;
import com.example.monoauction.common.enums.ItemCategory;
import com.example.monoauction.common.enums.ItemStatus;
import com.example.monoauction.item.dto.ItemSearchRequest;
import com.example.monoauction.item.dto.ItemSearchResponse;
import com.example.monoauction.item.service.ItemSearchService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.*;

@RestController
@RequestMapping("/api/search")
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
public class ItemSearchController {

    private final ItemSearchService searchService;

    @GetMapping("/items")
    public ResponseEntity<ApiResponse<ItemSearchResponse>> searchItems(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) ItemCategory category,
            @RequestParam(required = false) ItemStatus status,
            @RequestParam(required = false) BigDecimal minPrice,
            @RequestParam(required = false) BigDecimal maxPrice,
            @RequestParam(required = false) Long sellerId,
            @RequestParam(required = false) Long batchId,
            @RequestParam(required = false) Boolean hasBids,
            @RequestParam(required = false) Integer minBids,
            @RequestParam(required = false) Boolean hasReservePrice,
            @RequestParam(required = false, defaultValue = "date") String sortBy,
            @RequestParam(required = false, defaultValue = "DESC") String sortOrder,
            @RequestParam(required = false, defaultValue = "0") Integer page,
            @RequestParam(required = false, defaultValue = "20") Integer size) {

        ItemSearchRequest request = new ItemSearchRequest();
        request.setKeyword(keyword);
        request.setCategory(category);
        request.setStatus(status);
        request.setMinPrice(minPrice);
        request.setMaxPrice(maxPrice);
        request.setSellerId(sellerId);
        request.setBatchId(batchId);
        request.setHasBids(hasBids);
        request.setMinBids(minBids);
        request.setHasReservePrice(hasReservePrice);
        request.setSortBy(sortBy);
        request.setSortOrder(sortOrder);
        request.setPage(page);
        request.setSize(size);

        ItemSearchResponse response = searchService.searchItems(request);

        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PostMapping("/items")
    public ResponseEntity<ApiResponse<ItemSearchResponse>> searchItemsPost(
            @RequestBody ItemSearchRequest request) {

        ItemSearchResponse response = searchService.searchItems(request);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/suggestions")
    public ResponseEntity<ApiResponse<List<String>>> getSearchSuggestions(
            @RequestParam String q) {

        List<String> suggestions = searchService.getSearchSuggestions(q);
        return ResponseEntity.ok(ApiResponse.success(suggestions));
    }

    @GetMapping("/filter-options")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getFilterOptions(
            @RequestParam(required = false) ItemStatus status) {

        Map<String, Object> options = new HashMap<>();

        List<ItemCategory> categories = searchService.getAvailableCategories();
        options.put("categories", categories);

        Map<ItemCategory, Long> categoryCounts = searchService.getCategoryCount(status);
        options.put("categoryCounts", categoryCounts);

        Object[] priceRange = searchService.getPriceRange(status);
        if (priceRange != null && priceRange.length == 2) {
            Map<String, BigDecimal> prices = new HashMap<>();
            prices.put("min", (BigDecimal) priceRange[0]);
            prices.put("max", (BigDecimal) priceRange[1]);
            options.put("priceRange", prices);
        }

        options.put("statuses", Arrays.asList(ItemStatus.values()));

        List<Map<String, String>> sortOptions = new ArrayList<>();
        sortOptions.add(Map.of("value", "date", "label", "Newest First"));
        sortOptions.add(Map.of("value", "price", "label", "Price"));
        sortOptions.add(Map.of("value", "bids", "label", "Most Bids"));
        sortOptions.add(Map.of("value", "title", "label", "Name"));
        options.put("sortOptions", sortOptions);

        return ResponseEntity.ok(ApiResponse.success(options));
    }
}
