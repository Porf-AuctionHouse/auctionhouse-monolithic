package com.example.monoauction.bids.repository;

import com.example.monoauction.bids.model.Bid;
import com.example.monoauction.common.enums.BidStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface BidRepository extends JpaRepository<Bid, Long> {

    List<Bid> findByItemIdOrderByAmountDesc(Long itemId);

    List<Bid> findByItemIdOrderByBidTimeDesc(Long itemId);

    Optional<Bid> findTopByItemIdOrderByAmountDesc(Long itemId);

    List<Bid> findByBidderIdOrderByBidTimeDesc(Long bidderId);

    Optional<Bid> findTopByItemIdAndBidderId(Long itemId, Long bidderId);

    List<Bid> findByItemIdAndStatus(Long itemId, BidStatus status);

    List<Bid> findByItemIdAndStatusNot(Long itemId, BidStatus status);

    Long countByItemId(Long itemId);

    Long countByBidderId(Long bidderId);

    List<Bid> findByBidderIdAndStatus(Long bidderId, BidStatus status);

}
