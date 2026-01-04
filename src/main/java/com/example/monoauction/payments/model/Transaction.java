package com.example.monoauction.payments.model;

import com.example.monoauction.common.enums.PaymentMethod;
import com.example.monoauction.common.enums.TransactionStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "ah_transactions", indexes = {
        @Index(name = "idx_transaction_buyer", columnList = "buyer_id"),
        @Index(name = "idx_tranaction_seller", columnList = "seller_id"),
        @Index(name = "idx_tranaction_status", columnList = "status")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Transaction {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "auction_item_id", nullable = false)
    private Long auctionItemId;

    @Column(name = "buyer_id",nullable = false)
    private Long buyerId;

    @Column(name = "seller_id", nullable = false)
    private Long sellerId;

    @Column(name = "winning_bid_id")
    private Long winningBidId;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal amount;

    @Column(precision = 10, scale = 2)
    private BigDecimal platformFee = BigDecimal.ZERO;

    @Column(precision = 10, scale = 2)
    private BigDecimal sellerPayout;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private TransactionStatus status;

    @Enumerated(EnumType.STRING)
    @Column(length = 50)
    private PaymentMethod paymentMethod;

    @Column(unique = true, length = 100)
    private String transactionReference;

    @Column(columnDefinition = "TEXT")
    private String paymentDetails;

    private LocalDateTime paidAt;
    private LocalDateTime payoutProcessedAt;
    private LocalDateTime refundedAt;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;

}
