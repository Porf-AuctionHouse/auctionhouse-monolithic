package com.example.monoauction.item.repository;

import com.example.monoauction.common.enums.ItemCategory;
import com.example.monoauction.common.enums.ItemStatus;
import com.example.monoauction.item.model.AuctionItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AuctionItemRepository extends JpaRepository<AuctionItem, Long> {

    List<AuctionItem> findByBatchId(Long batchId);

    List<AuctionItem> findBySellerIdOrderByCreatedAtDesc(Long sellerId);

    List<AuctionItem> findByStatus(ItemStatus status);

    List<AuctionItem> findByBatchIdAndStatus(Long batchId, ItemStatus status);

    List<AuctionItem> findByBatchIdAndStatusIn(Long batchId, List<ItemStatus> status);

    List<AuctionItem> findByStatusOrderByCreatedAtDesc(ItemStatus status);

    List<AuctionItem> findByCategoryAndStatus(ItemCategory category, ItemStatus status);

    Long countBySellerId(Long sellerId);

    Long countByBatchIdAndStatus(Long batchId, ItemStatus status);

    List<AuctionItem> findByTitleContainingIgnoreCaseAndStatus(String title, ItemStatus status);

    List<AuctionItem> findByWinnerIdOrderByCreatedAtDesc(Long winnerId);
}
