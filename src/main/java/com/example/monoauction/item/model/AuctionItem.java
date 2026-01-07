package com.example.monoauction.item.model;

import com.example.monoauction.common.enums.ItemCategory;
import com.example.monoauction.common.enums.ItemStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Locale;

@Entity
@Table(name = "ah_auctionitems", indexes = {
        @Index(name = "idx_item_batch_status", columnList = "batch_id, status"),
        @Index(name = "idx_item_seller", columnList = "seller_id")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuctionItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "batch_id", nullable = false)
    private Long batchId;

    @Column(name = "seller_id", nullable = false)
    private Long sellerId;

    @Column(name = "reviewed_by")
    private Long reviewedBy;

    @Column(name = "winner_id")
    private Long winnerId;

    @Column(nullable = false, length = 200)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private ItemCategory category;

    @Column(columnDefinition = "TEXT")
    private String imageUrls;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal startingPrice;

    @Column(precision = 10, scale = 2)
    private BigDecimal reservePrice;

    @Column(precision = 10, scale = 2)
    private BigDecimal currentBid;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal bidIncrement = new BigDecimal("10.00");

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ItemStatus status;

    @Column(columnDefinition = "TEXT")
    private String rejectionReason;

    @Column(columnDefinition = "TEXT")
    private String adminNote;

    @Column(nullable = false)
    private Integer totalBids = 0;

    @Column(nullable = false)
    private Integer viewCount = 0;

    @Column(nullable = false)
    private Integer watchlistCount = 0;

    private LocalDateTime submittedAt;
    private LocalDateTime reviewedAt;
    private LocalDateTime approvedAt;
    private LocalDateTime auctionStartedAt;
    private LocalDateTime auctionEndedAt;
    private LocalDateTime soldAt;

    private boolean isDeleted;
    private LocalDateTime withdrawnAt;

    @Version
    private Long version;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;
}
