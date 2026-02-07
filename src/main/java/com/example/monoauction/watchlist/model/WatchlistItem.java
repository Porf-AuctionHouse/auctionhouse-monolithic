package com.example.monoauction.watchlist.model;

import com.example.monoauction.item.model.AuctionItem;
import com.example.monoauction.user.model.User;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "ah_watchlistitem",
        uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "item_id"}),
        indexes = {
                @Index(name = "idx_watchlist_user", columnList = "user_id"),
                @Index(name = "idx_watchlist_item", columnList = "item_id")
        }
)
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WatchlistItem {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", insertable = false, updatable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "item_id", insertable = false, updatable = false)
    private AuctionItem auctionItem;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime addedAt;

    @Column(name = "notify_on_bid")
    private Boolean notifyOnBid = true;

    @Column(name = "notify_on_price_drop")
    private Boolean notifyOnPriceDrop = false;
}
