package com.example.monoauction.item.controller;

import com.example.monoauction.common.dto.ApiResponse;
import com.example.monoauction.item.dto.ItemResponse;
import com.example.monoauction.item.dto.SubmitItemRequest;
import com.example.monoauction.item.dto.UpdateItemRequest;
import com.example.monoauction.item.model.AuctionItem;
import com.example.monoauction.item.service.ItemSubmissionService;
import com.example.monoauction.security.SecurityUtils;
import com.example.monoauction.user.model.User;
import com.example.monoauction.user.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/items")
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
public class ItemSubmissionController {
    private final ItemSubmissionService itemService;

    private final UserService userService;

    @PostMapping("/submit")
    public ResponseEntity<ApiResponse<ItemResponse>> submitItem(
            @Valid @RequestBody SubmitItemRequest request) {

        Long sellerId = SecurityUtils.getCurrentUserId();

        AuctionItem item = itemService.submitItem(
                sellerId,
                request.getTitle(),
                request.getDescription(),
                request.getCategory(),
                request.getStartingPrice(),
                request.getReservePrice(),
                request.getImageUrls()
        );

        User seller = userService.getUserById(sellerId);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(
                        "Item submitted successfully",
                        new ItemResponse(item, seller.getFullName())
                ));

    }

    @GetMapping("/my-submissions")
    public ResponseEntity<ApiResponse<List<ItemResponse>>> getMySubmissions() {
        Long sellerId = SecurityUtils.getCurrentUserId();
        List<AuctionItem> items = itemService.getMySubmissions(sellerId);
        User seller = userService.getUserById(sellerId);

        List<ItemResponse> itemResponses = items.stream()
                .map(item -> new ItemResponse(item, seller.getFullName()))
                .toList();

        return ResponseEntity.ok(ApiResponse.success(
                itemResponses
        ));

    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ItemResponse>> getItemById(
            @PathVariable Long id
    ){
        AuctionItem item = itemService.getItemById(id);
        User seller = userService.getUserById(item.getSellerId());
        return ResponseEntity.ok(ApiResponse.success(
                new ItemResponse(item, seller.getFullName())
        ));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<ItemResponse>> updateItem(
            @PathVariable Long id,
            @Valid @RequestBody UpdateItemRequest request
    ){
        Long sellerId = SecurityUtils.getCurrentUserId();
        AuctionItem item = itemService.updateItem(
                id,
                sellerId,
                request.getTitle(),
                request.getDescription(),
                request.getStartingPrice()
        );

        User seller = userService.getUserById(sellerId);

        return ResponseEntity.ok(ApiResponse.success(
                "Item updated successfully",
                new ItemResponse(item, seller.getFullName())
        ));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> withdrawSubmission(
            @PathVariable Long id){
        Long sellerId =SecurityUtils.getCurrentUserId();
        itemService.withdrawSubmission(id, sellerId);
        return ResponseEntity.ok(ApiResponse.success(
                "Item withdrawal successful",
                null
        ));
    }

}
