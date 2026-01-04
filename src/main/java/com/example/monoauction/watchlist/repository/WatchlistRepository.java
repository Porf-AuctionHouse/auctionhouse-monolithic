package com.example.monoauction.watchlist.repository;

import com.example.monoauction.watchlist.model.Watchlist;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface WatchlistRepository extends JpaRepository<Watchlist, Long> {

    List<Watchlist> findByUserIdOrderByAddedAtDesc(Long userId);

    Boolean existsByUserIdAndItemId(Long userId, Long itemId);

    Optional<Watchlist> findByUserIdAndItemId(Long userId, Long itemId);

    Long countByItemId(Long itemId);

    void deleteByUserIdAndItemId(Long userId, Long itemId);

}
