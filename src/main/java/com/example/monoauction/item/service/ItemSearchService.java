package com.example.monoauction.item.service;

import com.example.monoauction.common.enums.ItemCategory;
import com.example.monoauction.common.enums.ItemStatus;
import com.example.monoauction.file.service.FileStorageService;
import com.example.monoauction.item.dto.ItemResponse;
import com.example.monoauction.item.dto.ItemSearchRequest;
import com.example.monoauction.item.dto.ItemSearchResponse;
import com.example.monoauction.item.model.AuctionItem;
import com.example.monoauction.item.repository.AuctionItemRepository;
import com.example.monoauction.user.model.User;
import com.example.monoauction.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class ItemSearchService {

    private final AuctionItemRepository itemRepository;
    private final UserRepository userRepository;
    private final FileStorageService fileStorageService;

    public ItemSearchResponse searchItems(ItemSearchRequest request) {
        Pageable pageable = buildPageable(request);

        Page<AuctionItem> itemPage = executeSearch(request, pageable);

        List<ItemResponse> itemResponse = itemPage.getContent().stream()
                .map(this::convertToResponse)
                .toList();

        return buildSearchResponse(itemPage, itemResponse, request);
    }

    private Page<AuctionItem> executeSearch(ItemSearchRequest request, Pageable pageable) {

        if (hasMultipleFilters(request)) {
            return searchWithSpecification(request, pageable);
        }

        if (request.getKeyword() != null && !request.getKeyword().isEmpty()){
            if(request.getCategory() != null){
                return itemRepository.searchByKeywordAndCategory(
                        request.getKeyword(), request.getCategory(), pageable);
            }
            return itemRepository.searchByKeyword(request.getKeyword(), pageable);
        }

        if(request.getCategory() != null && request.getStatus() != null){
            return itemRepository.findByCategoryAndStatus(
                    request.getCategory(), request.getStatus(), pageable);
        }

        if(request.getStatus() != null){
            return itemRepository.findByStatus(request.getStatus(), pageable);
        }

        if (request.getCategory() != null) {
            return itemRepository.findByCategory(request.getCategory(), pageable);
        }

        return itemRepository.findAll(pageable);
    }

    private boolean hasMultipleFilters(ItemSearchRequest request){
        int filterCount = 0;

        if(request.getKeyword() != null && !request.getKeyword().isEmpty()) filterCount++;
        if(request.getCategory() != null) filterCount++;
        if(request.getStatus() != null) filterCount++;
        if (request.getMinPrice() != null || request.getMaxPrice() != null) filterCount++;
        if (request.getSellerId() != null) filterCount++;
        if (request.getBatchId() != null) filterCount++;
        if (request.getHasBids() != null) filterCount++;
        if (request.getMinBids() != null) filterCount++;

        return filterCount > 2;
    }

    private Page<AuctionItem> searchWithSpecification(
            ItemSearchRequest request, Pageable pageable){

        Specification<AuctionItem> spec = Specification.where((root, query, cb) -> cb.conjunction());

        if(request.getKeyword() != null && !request.getKeyword().isEmpty()){
            spec = spec.and(hasKeyword(request.getKeyword()));
        }

        if(request.getCategory() != null){
            spec = spec.and(hasCategory(request.getCategory()));
        }

        if(request.getStatus() != null){
            spec = spec.and(hasStatus(request.getStatus()));
        }

        if(request.getMinPrice() != null || request.getMaxPrice() != null){
            spec = spec.and(hasPriceRange(
                    request.getMinPrice(), request.getMaxPrice()));
        }

        if (request.getSellerId() != null) {
            spec = spec.and(hasSeller(request.getSellerId()));
        }

        if (request.getBatchId() != null) {
            spec = spec.and(hasBatch(request.getBatchId()));
        }

        if (request.getHasBids() != null && request.getHasBids()){
            spec = spec.and(hasBids());
        }

        if (request.getMinBids() != null){
            spec = spec.and(hasMinimumBids(request.getMinBids()));
        }

        if (request.getHasReservePrice() != null) {
            spec = spec.and(hasReservePrice(request.getHasReservePrice()));
        }

        return itemRepository.findAll(spec, pageable);
    }

    private Specification<AuctionItem> hasKeyword(String keyword) {
        return (root, query, cb) -> {
            String pattern = "%" + keyword.toLowerCase() + "%";
            return cb.or(
                    cb.like(cb.lower(root.get("title")), pattern),
                    cb.like(cb.lower(root.get("description")), pattern)
            );
        };
    }

    private Specification<AuctionItem> hasCategory(ItemCategory category) {
        return (root, query, cb) -> cb.equal(root.get("category"), category);
    }

    private Specification<AuctionItem> hasStatus(ItemStatus status) {
        return (root, query, cb) -> cb.equal(root.get("status"), status);
    }

    private Specification<AuctionItem> hasPriceRange(BigDecimal min, BigDecimal max) {
        return (root, query, cb) -> {
            if (min != null && max != null) {
                return cb.between(root.get("startingPrice"), min, max);
            } else if (min != null) {
                return cb.greaterThanOrEqualTo(root.get("startingPrice"), min);
            } else if (max != null) {
                return cb.lessThanOrEqualTo(root.get("startingPrice"), max);
            }
            return cb.conjunction();
        };
    }

    private Specification<AuctionItem> hasSeller(Long sellerId) {
        return (root, query, cb) -> cb.equal(root.get("sellerId"), sellerId);
    }

    private Specification<AuctionItem> hasBatch(Long batchId) {
        return (root, query, cb) -> cb.equal(root.get("batchId"), batchId);
    }

    private Specification<AuctionItem> hasBids() {
        return (root, query, cb) -> cb.greaterThan(root.get("totalBids"), 0);
    }

    private Specification<AuctionItem> hasMinimumBids(Integer minBids) {
        return (root, query, cb) ->
                cb.greaterThanOrEqualTo(root.get("totalBids"), minBids);
    }

    private Specification<AuctionItem> hasReservePrice(Boolean hasReserve) {
        return (root, query, cb) -> {
            if (hasReserve) {
                return cb.isNotNull(root.get("reservePrice"));
            } else {
                return cb.isNull(root.get("reservePrice"));
            }
        };
    }

    private Pageable buildPageable(ItemSearchRequest request) {
        int page = request.getPage() != null ? request.getPage() : 0;
        int size = request.getSize() != null ? request.getSize() : 20;

        size = Math.min(size, 100);

        Sort sort = buildSort(request.getSortBy(), request.getSortOrder());

        return PageRequest.of(page, size, sort);
    }

    private Sort buildSort(String sortBy, String sortOrder) {
        Sort.Direction direction = "DESC".equalsIgnoreCase(sortOrder) ?
                Sort.Direction.DESC : Sort.Direction.ASC;

        if (sortBy == null || sortBy.isEmpty()) {
            return Sort.by(Sort.Direction.DESC, "createdAt");
        }

        String fieldName = switch (sortBy.toLowerCase()) {
            case "price" -> "startingPrice";
            case "bids" -> "totalBids";
            case "date" -> "createdAt";
            case "title" -> "title";
            case "views" -> "viewCount";
            default -> "createdAt";
        };

        return Sort.by(direction, fieldName);
    }

    private ItemResponse convertToResponse(AuctionItem item) {
        User seller = userRepository.findById(item.getSellerId()).orElse(null);
        String sellerName = seller != null ? seller.getFullName() : "Unknown";

        ItemResponse response = new ItemResponse(item, sellerName);

        if (item.getImageUrls() != null && !item.getImageUrls().isEmpty()) {
            String imageUrls = fileStorageService.filenamesToUrls(item.getImageUrls());
            response.setImageUrls(imageUrls);
        }

        return response;
    }

    private ItemSearchResponse buildSearchResponse(
            Page<AuctionItem> itemsPage,
            List<ItemResponse> itemResponses,
            ItemSearchRequest request) {

        ItemSearchResponse response = new ItemSearchResponse();

        response.setItems(itemResponses);

        response.setCurrentPage(itemsPage.getNumber());
        response.setTotalPages(itemsPage.getTotalPages());
        response.setTotalItems(itemsPage.getTotalElements());
        response.setPageSize(itemsPage.getSize());
        response.setHasNext(itemsPage.hasNext());
        response.setHasPrevious(itemsPage.hasPrevious());

        response.setSearchKeyword(request.getKeyword());
        response.setAdditionalFilters(buildAppliedFiltersMap(request));

        response.setAvailableCategories(getAvailableCategories());
        response.setCategoryCount(getCategoryCount(request.getStatus()));

        Object[] priceRange = getPriceRange(request.getStatus());
        if (priceRange != null && priceRange.length == 2) {
            response.setMinPrice((BigDecimal) priceRange[0]);
            response.setMaxPrice((BigDecimal) priceRange[1]);
        }

        return response;
    }

    private Map<String, Object> buildAppliedFiltersMap(ItemSearchRequest request) {
        Map<String, Object> filters = new HashMap<>();

        if (request.getKeyword() != null) filters.put("keyword", request.getKeyword());
        if (request.getCategory() != null) filters.put("category", request.getCategory());
        if (request.getStatus() != null) filters.put("status", request.getStatus());
        if (request.getMinPrice() != null) filters.put("minPrice", request.getMinPrice());
        if (request.getMaxPrice() != null) filters.put("maxPrice", request.getMaxPrice());
        if (request.getSellerId() != null) filters.put("sellerId", request.getSellerId());
        if (request.getBatchId() != null) filters.put("batchId", request.getBatchId());
        if (request.getSortBy() != null) filters.put("sortBy", request.getSortBy());
        if (request.getSortOrder() != null) filters.put("sortOrder", request.getSortOrder());

        return filters;
    }

    public List<ItemCategory> getAvailableCategories() {
        return itemRepository.findAllCategories();
    }

    public Map<ItemCategory, Long> getCategoryCount(ItemStatus status) {
        ItemStatus searchStatus = status != null ? status : ItemStatus.LIVE;

        List<Object[]> results = itemRepository.countByCategory(searchStatus);

        Map<ItemCategory, Long> categoryCount = new HashMap<>();
        for (Object[] result : results) {
            categoryCount.put((ItemCategory) result[0], (Long) result[1]);
        }

        return categoryCount;
    }

    public Object[] getPriceRange(ItemStatus status) {
        ItemStatus searchStatus = status != null ? status : ItemStatus.LIVE;
        return itemRepository.findPriceRange(searchStatus);
    }

    public List<String> getSearchSuggestions(String prefix) {
        if (prefix == null || prefix.length() < 2) {
            return Collections.emptyList();
        }

        List<AuctionItem> items = itemRepository.findTop10ByTitleStartingWithIgnoreCase(prefix);

        return items.stream()
                .map(AuctionItem::getTitle)
                .distinct()
                .limit(5)
                .collect(Collectors.toList());
    }
}
