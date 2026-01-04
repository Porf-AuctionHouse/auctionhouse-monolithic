package com.example.monoauction.watchlist.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "ah_watchlist",
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
public class Watchlist {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "item_id", nullable = false)
    private Long itemId;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime addedAt;
}
