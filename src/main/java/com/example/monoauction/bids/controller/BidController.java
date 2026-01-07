package com.example.monoauction.bids.controller;

import com.example.monoauction.bids.dto.BidResponse;
import com.example.monoauction.bids.dto.PlaceBidRequest;
import com.example.monoauction.bids.model.Bid;
import com.example.monoauction.bids.service.BidService;
import com.example.monoauction.common.dto.ApiResponse;
import com.example.monoauction.security.SecurityUtils;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/bids")
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
public class BidController {

    private final BidService bidService;

    @PostMapping("/item/{itemId}")
    public ResponseEntity<ApiResponse<BidResponse>> placeBid(
            @PathVariable Long itemId,
            @Valid @RequestBody PlaceBidRequest request
            ){
        Long bidderId = SecurityUtils.getCurrentUserId();
        Bid bid = bidService.placeBid(itemId, bidderId, request.getAmount());

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Bid placed successfully", new BidResponse(bid)));
    }

    @GetMapping("/item/{itemId}/history")
    public ResponseEntity<ApiResponse<List<BidResponse>>> getBidHistory(
            @PathVariable Long itemId
    ){
        List<Bid> bids = bidService.getBidHistory(itemId);
        List<BidResponse> responses = bids.stream()
                .map(BidResponse::new)
                .toList();

        return ResponseEntity.ok(ApiResponse.success(responses));
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<ApiResponse<List<BidResponse>>> getUserBids(
            @PathVariable Long userId
    ){
        List<Bid> bids = bidService.getUserBid(userId);
        List<BidResponse> responses = bids.stream()
                .map(BidResponse::new)
                .toList();

        return ResponseEntity.ok(ApiResponse.success(responses));
    }

    @GetMapping("/items/{itemId}/minimum")
    public ResponseEntity<ApiResponse<Map<String, BigDecimal>>> getMinimumBid(
            @PathVariable Long itemId
    ){
        BigDecimal minimumBid = bidService.calculatedMinimumBid(itemId);
        Map<String, BigDecimal> response = new HashMap<>();
        response.put("minimumBid", minimumBid);

        return ResponseEntity.ok(ApiResponse.success(response));
    }

}
