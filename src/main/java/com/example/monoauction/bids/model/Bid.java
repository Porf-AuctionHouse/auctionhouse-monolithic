package com.example.monoauction.bids.model;

import com.example.monoauction.common.enums.BidStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.springframework.data.annotation.CreatedDate;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "ah_bids", indexes = {
        @Index(name = "idx_batch_item_amount", columnList = "item_id, amount DESC"),
        @Index(name = "idx_batch_bidder_time", columnList = "bidder_id, bid_time DESC"),
        @Index(name = "idx_batch_item_status", columnList = "item_id, status")})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Bid {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long itemId;

    private Long bidderId;

    private BigDecimal amount;

    private BidStatus status;

    private LocalDateTime bidTime;

    private String bidderName;

    private Long version;

    private String ipAdderss;

    private String userAgent;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;
}
