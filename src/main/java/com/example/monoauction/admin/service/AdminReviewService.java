package com.example.monoauction.admin.service;

import com.example.monoauction.batch.model.AuctionBatch;
import com.example.monoauction.batch.service.AuctionBatchService;
import com.example.monoauction.common.enums.ItemStatus;
import com.example.monoauction.common.enums.UserRole;
import com.example.monoauction.item.model.AuctionItem;
import com.example.monoauction.item.repository.AuctionItemRepository;
import com.example.monoauction.notifications.event.ItemApprovedEvent;
import com.example.monoauction.notifications.event.ItemRejectedEvent;
import com.example.monoauction.user.model.User;
import com.example.monoauction.user.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@Transactional
@RequiredArgsConstructor
public class AdminReviewService {
    private final AuctionItemRepository itemRepository;
    private final AuctionBatchService batchService;
    private final UserRepository userRepository;
    private final ApplicationEventPublisher eventPublisher;

    public List<AuctionItem> getItemForReview(){
     AuctionBatch currentBatch = batchService.getCurrentBatch();
     List<ItemStatus> reviewStatus = Arrays.asList(
             ItemStatus.SUBMITTED,
             ItemStatus.UNDER_REVIEW
     );
     return itemRepository.findByBatchIdAndStatusIn(currentBatch.getId(),reviewStatus);
    }

    public AuctionItem approveItem(Long itemId, Long adminId, String notes){
        AuctionItem item = itemRepository.findById(itemId)
                .orElseThrow(() -> new RuntimeException("Item Not Found With These Details"));

        User admin = userRepository.findById(adminId)
                .orElseThrow(() -> new RuntimeException("Admin Not Found With These Details"));


        if(admin.getRole() != UserRole.ADMIN){
            throw new RuntimeException("Only Admin Can Approve Items");
        }

        item.setStatus(ItemStatus.APPROVED);
        item.setReviewedBy(admin.getId());
        item.setReviewedAt(LocalDateTime.now());
        item.setApprovedAt(LocalDateTime.now());
        item.setAdminNote(notes);

        AuctionItem savedItem = itemRepository.save(item);

        batchService.incrementItemApproved(item.getBatchId());
        eventPublisher.publishEvent(new ItemApprovedEvent(savedItem));

        return savedItem;
    }

    public AuctionItem rejectItem(Long itemId, Long adminId, String reason){
        AuctionItem item = itemRepository.findById(itemId)
                .orElseThrow(() -> new RuntimeException("Item Not Found With These Details"));

        User admin = userRepository.findById(adminId)
                .orElseThrow(() -> new RuntimeException("Admin Not Found With These Details"));

        if(admin.getRole() != UserRole.ADMIN){
            throw new RuntimeException("Only Admin Can Reject Items");
        }

        item.setStatus(ItemStatus.REJECTED);
        item.setReviewedBy(admin.getId());
        item.setReviewedAt(LocalDateTime.now());
        item.setRejectionReason(reason);

        AuctionItem savedItem = itemRepository.save(item);

        batchService.incrementItemRejected(item.getBatchId());
        eventPublisher.publishEvent(new ItemRejectedEvent(savedItem, reason));

        return savedItem;

    }

    public AuctionItem requestChanges(Long itemId, Long adminId, String feedback){
        AuctionItem item = itemRepository.findById(itemId)
                .orElseThrow(() -> new RuntimeException("Item Not Found With These Details"));

        User admin = userRepository.findById(adminId)
                .orElseThrow(() -> new RuntimeException("Admin Not Found With These Details"));

        if(admin.getRole() != UserRole.ADMIN){
            throw new RuntimeException("Only Admin Can Request Changes");
        }

        item.setStatus(ItemStatus.CHANGES_REQUESTED);
        item.setReviewedBy(admin.getId());
        item.setReviewedAt(LocalDateTime.now());
        item.setAdminNote(feedback);

        return itemRepository.save(item);
    }

    public List<AuctionItem> getAllItemsInCurrentBatch(){
        AuctionBatch currentBatch = batchService.getCurrentBatch();
        return itemRepository.findByBatchId(currentBatch.getId());
    }

    public Map<String, Long> getReviewStats(){
        AuctionBatch currentBatch = batchService.getCurrentBatch();

        Map<String, Long> stats = new HashMap<>();

        stats.put("submitted", itemRepository.countByBatchIdAndStatus(
                currentBatch.getId(), ItemStatus.SUBMITTED));

        stats.put("underReview", itemRepository.countByBatchIdAndStatus(
                currentBatch.getId(), ItemStatus.UNDER_REVIEW));

        stats.put("approved", itemRepository.countByBatchIdAndStatus(
                currentBatch.getId(), ItemStatus.APPROVED));

        stats.put("rejected", itemRepository.countByBatchIdAndStatus(
                currentBatch.getId(), ItemStatus.REJECTED));

        return stats;
    }
}
