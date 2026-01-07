package com.example.monoauction.admin.controller;


import com.example.monoauction.admin.dto.ApprovalRequest;
import com.example.monoauction.admin.dto.RejectionRequest;
import com.example.monoauction.admin.dto.ReviewRequest;
import com.example.monoauction.admin.service.AdminReviewService;
import com.example.monoauction.common.dto.ApiResponse;
import com.example.monoauction.item.dto.ItemResponse;
import com.example.monoauction.item.model.AuctionItem;
import com.example.monoauction.security.SecurityUtils;
import com.example.monoauction.user.model.User;
import com.example.monoauction.user.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/items")
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
public class AdminReviewController {

    private final AdminReviewService reviewService;

    private final UserService userService;

    @GetMapping("/pending")
    public ResponseEntity<ApiResponse<List<ItemResponse>>> getItemForReview() {
        List<AuctionItem> items = reviewService.getItemForReview();

        List<ItemResponse> responses = items.stream()
                .map(item -> {
                    User seller = userService.getUserById(item.getSellerId());
                    return new ItemResponse(item, seller.getFullName());
                })
                .toList();

        return ResponseEntity.ok(ApiResponse.success(responses));
    }

    @PostMapping("/{id}/approve")
    public ResponseEntity<ApiResponse<ItemResponse>> approveItem(
            @PathVariable Long id,
            @RequestBody(required = false)ApprovalRequest request
            ){
        Long adminId = SecurityUtils.getCurrentUserId();
        String notes = request != null ? request.getNotes() : "Approved By Admin";
        AuctionItem item = reviewService.approveItem(id, adminId, notes);
        User seller = userService.getUserById(item.getSellerId());

        return ResponseEntity.ok(ApiResponse.success("Item approved successfully",
                new ItemResponse(item, seller.getFullName())));
    }

    @PostMapping("/{id}/reject")
    public ResponseEntity<ApiResponse<ItemResponse>> rejectItem(
            @PathVariable Long id,
            @Valid @RequestBody RejectionRequest request
            ){
        Long adminId = SecurityUtils.getCurrentUserId();
        AuctionItem item = reviewService.rejectItem(id, adminId, request.getReason());
        User seller = userService.getUserById(item.getSellerId());

        return ResponseEntity.ok(ApiResponse.success("Item Rejected", new ItemResponse(item, seller.getFullName())));
    }

    @PostMapping("/{id}/request-changes")
    public ResponseEntity<ApiResponse<ItemResponse>> requestChanges(
            @PathVariable Long id,
            @Valid @RequestBody ReviewRequest request
            )  {
        Long adminId = SecurityUtils.getCurrentUserId();
        AuctionItem item = reviewService.requestChanges(id, adminId, request.getNotes());
        User seller = userService.getUserById(item.getSellerId());

        return ResponseEntity.ok(ApiResponse.success("Changes requested",
                new ItemResponse(item, seller.getFullName())));
    }


    @GetMapping("/stats")
    public ResponseEntity<ApiResponse<Map<String, Long>>> getReviewStats(){
        Map<String, Long> stats = reviewService.getReviewStats();
        return ResponseEntity.ok(ApiResponse.success(stats));
    }
}
