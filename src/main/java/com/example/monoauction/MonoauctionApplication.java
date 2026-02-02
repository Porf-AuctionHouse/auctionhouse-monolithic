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
import org.springframework.context.annotation.Primary;
import org.springframework.core.task.TaskExecutor;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

@SpringBootApplication
@EnableScheduling
@EnableAsync
public class MonoauctionApplication {

	public static void main(String[] args) {
		SpringApplication.run(MonoauctionApplication.class, args);
	}

    @Bean(name = "taskExecutor")
    @Primary
    public TaskExecutor taskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(5);
        executor.setMaxPoolSize(10);
        executor.setQueueCapacity(25);
        executor.setThreadNamePrefix("Async-");
        executor.initialize();
        return executor;
    }
}
