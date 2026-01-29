package com.example.monoauction.notifications.listener;

import com.example.monoauction.batch.model.AuctionBatch;
import com.example.monoauction.batch.service.AuctionBatchService;
import com.example.monoauction.bids.model.Bid;
import com.example.monoauction.common.dto.ApiResponse;
import com.example.monoauction.notifications.service.WebSocketNotificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/ws-test")
@CrossOrigin(origins = "*")
public class WebSocketTestController {

    @Autowired
    private WebSocketNotificationService webSocketService;

    @Autowired
    private AuctionBatchService batchService;

    @PostMapping("/test-bid")
    public ResponseEntity<ApiResponse<String>> testBidUpdate(@RequestParam Long itemId) {
        Bid testBid = new Bid();
        testBid.setId(999L);
        testBid.setItemId(itemId);
        testBid.setBidderId(1L);
        testBid.setBidderName("Test User");
        testBid.setAmount(new BigDecimal("150.00"));
        testBid.setBidTime(LocalDateTime.now());

        webSocketService.sendBidUpdate(itemId, testBid);

        return ResponseEntity.ok(ApiResponse.success("Test bid update sent"));
    }

    // Test sending auction status
    @PostMapping("/test-auction-status")
    public ResponseEntity<ApiResponse<String>> testAuctionStatus() {
        AuctionBatch batch = batchService.getCurrentBatch();
        webSocketService.sendAuctionStatusUpdate(batch, "Test auction status message");

        return ResponseEntity.ok(ApiResponse.success("Test auction status sent"));
    }
}
