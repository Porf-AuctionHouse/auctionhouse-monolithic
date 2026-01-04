package com.example.monoauction;

import com.example.monoauction.batch.repository.AuctionBatchRepository;
import com.example.monoauction.bids.repository.BidRepository;
import com.example.monoauction.item.repository.AuctionItemRepository;
import com.example.monoauction.notifications.repository.NotificationRepository;
import com.example.monoauction.payments.repository.TransactionRepository;
import com.example.monoauction.user.repository.UserRepository;
import com.example.monoauction.watchlist.repository.WatchlistRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
@EnableJpaAuditing
public class MonoauctionApplication {

	public static void main(String[] args) {
		SpringApplication.run(MonoauctionApplication.class, args);
	}

//    @Bean
//    CommandLineRunner testRepositories(
//            UserRepository userRepository,
//            AuctionBatchRepository batchRepository,
//            AuctionItemRepository itemRepository,
//            BidRepository bidRepository,
//            NotificationRepository notificationRepository,
//            TransactionRepository transactionRepository,
//            WatchlistRepository watchlistRepository
//    ) {
//        return args -> {
//
//            System.out.println("=== Testing Repositories ===");
//
//            System.out.println("Users: " + userRepository.count());
//            System.out.println("Batch: " + batchRepository.count());
//            System.out.println("Items: " + itemRepository.count());
//            System.out.println("Bids: " + bidRepository.count());
//            System.out.println("Transactions: " + transactionRepository.count());
//            System.out.println("Watchlist: " + watchlistRepository.count());
//
//            System.out.println("=== End of Testing ===");
//
//        };
//    }
}
