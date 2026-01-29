package com.example.monoauction.notifications.service;

import com.example.monoauction.batch.model.AuctionBatch;
import com.example.monoauction.bids.dto.AuctionStatusMessage;
import com.example.monoauction.bids.dto.BidUpdateMessage;
import com.example.monoauction.bids.dto.ItemStatusUpdateMessage;
import com.example.monoauction.bids.model.Bid;
import com.example.monoauction.common.enums.BatchStatus;
import com.example.monoauction.common.enums.ItemStatus;
import com.example.monoauction.item.model.AuctionItem;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.Map;

@Service
@Slf4j
@RequiredArgsConstructor
public class WebSocketNotificationService {

    private final SimpMessagingTemplate messagingTemplate;

    public void sendBidUpdate(Long itemId, Bid bid) {
        BidUpdateMessage message = new BidUpdateMessage();
        message.setBidId(itemId);
        message.setBidId(bid.getId());
        message.setBidderName(bid.getBidderName());
        message.setAmount(bid.getAmount());
        message.setBidTime(bid.getBidTime());
        message.setStatus("NEW_BID");

        String destination = "/topic/item/" + itemId;
        messagingTemplate.convertAndSend(destination, message);

        log.info("WebSocket: Sent bid update to {}", destination);
    }

    public void sendOutbidNotification(Long userId, Long itemId, BigDecimal newBidAmount) {
        Map<String, Object> message = new HashMap<>();
        message.put("type", "OUTBID");
        message.put("itemId", itemId);
        message.put("newBidAmount", newBidAmount);
        message.put("timestamp", LocalDateTime.now());
        message.put("message", "You have been outbid!");

        // Send to specific user queue
        String destination = "/queue/user/" + userId;
        messagingTemplate.convertAndSend(destination, (Object) message);

        log.info("WebSocket: Sent outbid notification to user {}", destination);

    }

    public void sendItemStatusUpdate(AuctionItem item, ItemStatus oldStatus) {
        ItemStatusUpdateMessage message = new ItemStatusUpdateMessage();
        message.setItemId(item.getId());
        message.setTitle(item.getTitle());
        message.setOldStatus(oldStatus);
        message.setNewStatus(item.getStatus());
        message.setTimestamp(LocalDateTime.now());
        message.setMessage("Item status change: " + oldStatus + " -> " + item.getStatus());

        String destination = "/topic/item/" + item.getId();
        messagingTemplate.convertAndSend(destination, message);

        log.info("WebSocket: Sent item status update for item {}", destination);
    }

    public void sendAuctionStatusUpdate(AuctionBatch batch, String message){
        AuctionStatusMessage statusMessage = new AuctionStatusMessage();
        statusMessage.setBatchCode(batch.getBatchCode());
        statusMessage.setStatus(batch.getStatus());
        statusMessage.setTimestamp(LocalDateTime.now());
        statusMessage.setMessage(message);

        if (batch.getStatus() == BatchStatus.LIVE){
            long minutesRemaining = ChronoUnit.MINUTES.between(
                    LocalDateTime.now(),
                    batch.getAuctionEndTime()
            );
            statusMessage.setMinutesRemaining(minutesRemaining);
        }

        String destination = "/topic/auction/" + batch.getId();
        messagingTemplate.convertAndSend(destination, statusMessage);

        log.info("WebSocket: Sent auction status update {}", destination);
    }

    public void sendCountdowndUpdate(Long itemId, Long secondsRemaining) {
        Map<String, Object> message = new HashMap<>();
        message.put("itemId", itemId);
        message.put("secondsRemaining", secondsRemaining);
        message.put("timestamp", LocalDateTime.now());

        messagingTemplate.convertAndSend("/topic/item/" + itemId + "/countdown", (Object) message);

        log.info("WebSocket: Sent countdown update for item {}", itemId);
    }

}
