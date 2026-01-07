package com.example.monoauction.batch.controller;

import com.example.monoauction.batch.dto.BatchResponse;
import com.example.monoauction.batch.model.AuctionBatch;
import com.example.monoauction.batch.service.AuctionBatchService;
import com.example.monoauction.common.dto.ApiResponse;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/batches")
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
public class BatchController {

    private final AuctionBatchService batchService;

    @GetMapping("/current")
    public ResponseEntity<ApiResponse<BatchResponse>> getCurrentBatch(){
        AuctionBatch batch = batchService.getCurrentBatch();
        return ResponseEntity.ok(ApiResponse.success(new BatchResponse(batch)));
    }

    @PostMapping("/create-test-batch")
    public ResponseEntity<ApiResponse<BatchResponse>> createTestBatch() {
        AuctionBatch batch = batchService.createTestBatch();
        return ResponseEntity.ok(ApiResponse.success(
                "Test batch created - will transition in minutes",
                new BatchResponse(batch)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<BatchResponse>> getBatchById(@PathVariable Long id){
        AuctionBatch batch = batchService.getBatchById(id);
        return ResponseEntity.ok(ApiResponse.success(new BatchResponse(batch)));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<Page<BatchResponse>>> getAllBatches(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ){
        Pageable pageable = PageRequest.of(page, size);
        Page<AuctionBatch> batches = batchService.getAllBatches(pageable);
        Page<BatchResponse> responses = batches.map(BatchResponse::new);

        return ResponseEntity.ok(ApiResponse.success(responses));
    }

    @GetMapping("/submission-status")
    public ResponseEntity<ApiResponse<Map<String, Boolean>>> getSubmissionStatus(){
        Map<String, Boolean> status = new HashMap<>();
        status.put("isSubmissionOpen", batchService.isSubmissionOpen());
        status.put("isReviewPhaseActive", batchService.isReviewPhaseActive());
        status.put("isAuctionLive", batchService.isAuctionLive());

        return ResponseEntity.ok(ApiResponse.success(status));
    }
}
