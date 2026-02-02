package com.example.monoauction.batch.model;

import com.example.monoauction.common.enums.BatchStatus;
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
@Table(name = "ah_auctionbatch")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuctionBatch {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false,unique = true,length = 50)
    private String batchCode;

    @Column(nullable = false)
    private Integer weekNumber;

    @Column(nullable = false)
    private Integer year;

    @Column(nullable = false)
    private Boolean isDeleted = false;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private BatchStatus status;

    @Column(nullable = false)
    private LocalDateTime submissionStartDate;

    @Column(nullable = false)
    private LocalDateTime submissionEndDate;

    @Column(nullable = false)
    private LocalDateTime reviewStartDate;

    @Column(nullable = false)
    private LocalDateTime reviewEndDate;

    @Column(nullable = false)
    private LocalDateTime auctionStartTime;

    @Column(nullable = false)
    private LocalDateTime auctionEndTime;

    @Column(nullable = false)
    private Integer totalItemsSubmitted = 0;

    @Column(nullable = false)
    private Integer totalItemsApproved = 0;

    @Column(nullable = false)
    private Integer totalItemsRejected = 0;

    @Column(nullable = false)
    private Integer totalItemsSold = 0;

    @Column(precision = 12, scale = 2)
    private BigDecimal totalRevenue = BigDecimal.ZERO;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;

}
