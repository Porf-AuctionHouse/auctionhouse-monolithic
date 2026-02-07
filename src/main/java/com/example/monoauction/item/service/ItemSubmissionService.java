package com.example.monoauction.item.service;

import com.example.monoauction.batch.model.AuctionBatch;
import com.example.monoauction.batch.repository.AuctionBatchRepository;
import com.example.monoauction.batch.service.AuctionBatchService;
import com.example.monoauction.common.enums.ItemCategory;
import com.example.monoauction.common.enums.ItemStatus;
import com.example.monoauction.common.enums.UserRole;
import com.example.monoauction.common.execptions.BusinessException;
import com.example.monoauction.common.execptions.ResourceNotFoundException;
import com.example.monoauction.item.dto.SubmitItemRequest;
import com.example.monoauction.item.model.AuctionItem;
import com.example.monoauction.item.repository.AuctionItemRepository;
import com.example.monoauction.notifications.event.ItemSubmittedEvent;
import com.example.monoauction.user.model.User;
import com.example.monoauction.user.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
@Transactional
@RequiredArgsConstructor
public class ItemSubmissionService {

    private final AuctionItemRepository itemRepository;
    private final AuctionBatchService batchService;
    private final AuctionBatchRepository batchRepository;
    private final UserRepository userRepository;
    private final ApplicationEventPublisher eventPublisher;

    public AuctionItem submitItem(Long sellerId, String title, String description,
                                  ItemCategory category, BigDecimal startingPrice,
                                  BigDecimal reservePrice, String imageUrls){

        if(!batchService.isSubmissionOpen()){
            throw new RuntimeException("Submission Not Open");
        }

        AuctionBatch currentBatch = batchService.getCurrentBatch();

        User seller = userRepository.findById(sellerId)
                .orElseThrow(() -> new RuntimeException("Seller Not Found With These Details"));

        if(seller.getRole() != UserRole.SELLER && seller.getRole() != UserRole.ADMIN){
            throw new RuntimeException("Only Sellers Can Submit Items");
        }

        AuctionItem item = new AuctionItem();

        item.setBatchId(currentBatch.getId());
        item.setSellerId(sellerId);
        item.setTitle(title);
        item.setDescription(description);
        item.setCategory(category);
        item.setStartingPrice(startingPrice);
        item.setReservePrice(reservePrice);
        item.setImageUrls(imageUrls);
        item.setStatus(ItemStatus.SUBMITTED);
        item.setSubmittedAt(LocalDateTime.now());
        item.setTotalBids(0);

        AuctionItem savedItem = itemRepository.save(item);

        batchService.incrementItemSubmitted(currentBatch.getId());

        eventPublisher.publishEvent(new ItemSubmittedEvent(savedItem));

        return savedItem;

    }

    public AuctionItem submitItemWithImages(Long sellerId, SubmitItemRequest request,
                                            List<String> uploadedFilenames) {

        if (!batchService.isSubmissionOpen()) {
            throw new BusinessException("Item submission is closed.");
        }

        AuctionBatch currentBatch = batchService.getCurrentBatch();

        User seller = userRepository.findById(sellerId)
                .orElseThrow(() -> new ResourceNotFoundException("Seller not found"));

        if (seller.getRole() != UserRole.SELLER && seller.getRole() != UserRole.ADMIN) {
            throw new BusinessException("Only sellers can submit items");
        }

        AuctionItem item = new AuctionItem();
        item.setBatchId(currentBatch.getId());
        item.setSellerId(sellerId);
        item.setTitle(request.getTitle());
        item.setDescription(request.getDescription());
        item.setCategory(request.getCategory());
        item.setStartingPrice(request.getStartingPrice());
        item.setReservePrice(request.getReservePrice());

        if (uploadedFilenames != null && !uploadedFilenames.isEmpty()) {
            item.setImageUrls(String.join(",", uploadedFilenames));
        }

        item.setStatus(ItemStatus.SUBMITTED);
        item.setSubmittedAt(LocalDateTime.now());
        item.setTotalBids(0);

        AuctionItem savedItem = itemRepository.save(item);

        batchService.incrementItemSubmitted(currentBatch.getId());

        eventPublisher.publishEvent(new ItemSubmittedEvent(savedItem));

        return savedItem;
    }

    public List<AuctionItem> getMySubmissions(Long sellerId){
        return itemRepository.findBySellerIdOrderByCreatedAtDesc(sellerId);
    }

    public AuctionItem getItemById(Long itemId){
        return itemRepository.findById(itemId)
                .orElseThrow(() -> new RuntimeException("Item Not Found With These Details"));
    }

    public AuctionItem updateItem(Long itemId, Long sellerId, String title, String description, BigDecimal startingPrice){

        AuctionItem item = getItemById(itemId);

        if(!item.getSellerId().equals(sellerId)){
            throw new RuntimeException("You Can Only Update Your Own Items");
        }


        if(item.getStatus() != ItemStatus.SUBMITTED && item.getStatus() != ItemStatus.CHANGES_REQUESTED){
            throw new RuntimeException("Cannot Update Item After Review");
        }

        item.setTitle(title);
        item.setDescription(description);
        item.setStartingPrice(startingPrice);

        return itemRepository.save(item);
    }

    public void withdrawSubmission(Long itemId, Long sellerId){
        AuctionItem item = getItemById(itemId);

        if(!item.getSellerId().equals(sellerId)){
            throw new RuntimeException("You Can Only Withdraw Your Own Items");
        }

        if(item.getStatus() != ItemStatus.SUBMITTED){
            throw new RuntimeException("Cannot Withdraw Item After Review");
        }

        item.setDeleted(true);
        item.setStatus(ItemStatus.WITHDRAWN);
        item.setWithdrawnAt(LocalDateTime.now());

        itemRepository.save(item);

        AuctionBatch batch = batchService.getBatchById(item.getBatchId());
        batch.setTotalItemsSubmitted(batch.getTotalItemsSubmitted() - 1);
        batchRepository.save(batch);

    }

    public List<AuctionItem> getItemsByStatus(ItemStatus status){
        return itemRepository.findByStatusOrderByCreatedAtDesc(status);
    }

    public List<AuctionItem> getItemsByCategoryAndStatus(ItemCategory category, ItemStatus status){
        return itemRepository.findByCategoryAndStatus(category, status);
    }


}
