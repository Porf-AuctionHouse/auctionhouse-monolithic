package com.example.monoauction.item.controller;

import com.example.monoauction.batch.model.AuctionBatch;
import com.example.monoauction.batch.service.AuctionBatchService;
import com.example.monoauction.common.dto.ApiResponse;
import com.example.monoauction.common.enums.ItemStatus;
import com.example.monoauction.common.execptions.ResourceNotFoundException;
import com.example.monoauction.file.service.FileStorageService;
import com.example.monoauction.item.dto.ItemResponse;
import com.example.monoauction.item.model.AuctionItem;
import com.example.monoauction.item.repository.AuctionItemRepository;
import com.example.monoauction.security.SecurityUtils;
import com.example.monoauction.user.model.User;
import com.example.monoauction.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.parameters.P;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/auction")
@CrossOrigin("*")
@RequiredArgsConstructor
public class ItemViewController {

    private final AuctionItemRepository itemRepository;

    private final AuctionBatchService batchService;

    private final UserService userService;
    private final FileStorageService fileStorageService;

    @GetMapping("/items")
    public ResponseEntity<ApiResponse<List<ItemResponse>>> getLiveItems(){
        List<AuctionItem> items = itemRepository.findByStatusOrderByCreatedAtDesc(ItemStatus.LIVE);

        List<ItemResponse> responses = items.stream()
                .map(item -> {
                        User seller = userService.getUserById(item.getSellerId());
                        return new ItemResponse(item, seller.getFullName());
                })
                .toList();

        return ResponseEntity.ok(ApiResponse.success(responses));
    }

    @GetMapping("/items/{id}/with-images")
    public ResponseEntity<ApiResponse<ItemResponse>> getItemWithImages(@PathVariable Long id) {
        AuctionItem item = itemRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Item not found"));

        User seller = userService.getUserById(item.getSellerId());
        ItemResponse response = new ItemResponse(item, seller.getFullName());

        // Convert filenames to full URLs
        if (item.getImageUrls() != null && !item.getImageUrls().isEmpty()) {
            String imageUrls = fileStorageService.filenamesToUrls(item.getImageUrls());
            response.setImageUrls(imageUrls);
        }

        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/results")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getAuctionResults(){
        AuctionBatch currentBatch = batchService.getCurrentBatch();

        List<AuctionItem> soldItems = itemRepository
                .findByBatchIdAndStatus(currentBatch.getId(), ItemStatus.SOLD);

        List<AuctionItem> unsoldItems = itemRepository
                .findByBatchIdAndStatus(currentBatch.getId(), ItemStatus.UNSOLD);

        Map<String, Object> results = new HashMap<>();
        results.put("batchCode", currentBatch.getBatchCode());
        results.put("status", currentBatch.getStatus());
        results.put("totalSold", soldItems.size());
        results.put("totalUnsold", unsoldItems.size());
        results.put("totalRevenue", currentBatch.getTotalRevenue());

        List<ItemResponse> soldResponse = soldItems.stream()
                .map(item ->{
                    User seller = userService.getUserById(item.getSellerId());
                    return new ItemResponse(item, seller.getFullName());
                })
                .toList();

        results.put("soldItems", soldResponse);

        return ResponseEntity.ok(ApiResponse.success(results));
    }

    @GetMapping("/my-wins")
    public ResponseEntity<ApiResponse<List<ItemResponse>>> getMyWins(){
        Long userId = SecurityUtils.getCurrentUserId();

        List<AuctionItem> wonItems = itemRepository.findByWinnerIdOrderByCreatedAtDesc(userId);

        List<ItemResponse> response = wonItems.stream()
                .map(item -> {
                    User seller = userService.getUserById(item.getSellerId());
                    return new ItemResponse(item, seller.getFullName());
                })
                .toList();

        return ResponseEntity.ok(ApiResponse.success(response));
    }

}
