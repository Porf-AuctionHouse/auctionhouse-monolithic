package com.example.monoauction.item.dto;

import com.example.monoauction.common.enums.ItemCategory;
import com.example.monoauction.common.enums.ItemStatus;
import com.example.monoauction.item.model.AuctionItem;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ItemResponse {
    private Long id;
    private Long batchId;
    private Long sellerId;
    private String sellerName;
    private String title;
    private String description;
    private ItemCategory category;
    private String imageUrls;
    private Map<String, List<String>> imageSizes;
    private BigDecimal startingPrice;
    private BigDecimal reservePrice;
    private BigDecimal currentBid;
    private BigDecimal bidIncrement;
    private ItemStatus status;
    private String rejectionReason;
    private String adminNotes;
    private Integer totalBids;
    private Integer viewCount;
    private LocalDateTime submittedAt;
    private LocalDateTime createdAt;

    public ItemResponse(AuctionItem item, String sellerName){
        this.id = item.getId();
        this.batchId = item.getBatchId();
        this.sellerId = item.getSellerId();
        this.sellerName = sellerName;
        this.title = item.getTitle();
        this.description = item.getDescription();
        this.category = item.getCategory();
        this.imageUrls = item.getImageUrls();
        this.startingPrice = item.getStartingPrice();
        this.reservePrice = item.getReservePrice();
        this.currentBid = item.getCurrentBid();
        this.bidIncrement = item.getBidIncrement();
        this.status = item.getStatus();
        this.rejectionReason = item.getRejectionReason();
        this.adminNotes = item.getAdminNote();
        this.totalBids = item.getTotalBids();
        this.viewCount = item.getViewCount();
        this.submittedAt = item.getSubmittedAt();
        this.createdAt = item.getCreatedAt();

    }
}
