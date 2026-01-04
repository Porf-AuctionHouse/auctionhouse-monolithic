package com.example.monoauction.payments.repository;

import com.example.monoauction.common.enums.TransactionStatus;
import com.example.monoauction.payments.model.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface TransactionRepository extends JpaRepository<Transaction, Long> {

    Optional<Transaction> findByAuctionItemId(Long auctionItemId);

    List<Transaction> findByBuyerIdOrderByCreatedAtDesc(Long buyerId);

    List<Transaction> findBySellerIdOrderByCreatedAtDesc(Long sellerId);

    List<Transaction> findByStatus(TransactionStatus status);

    Optional<Transaction> findByTransactionReference(String reference);

    Long countByBuyerId(Long buyerId);

    Long countBySellerId(Long sellerId);
}
