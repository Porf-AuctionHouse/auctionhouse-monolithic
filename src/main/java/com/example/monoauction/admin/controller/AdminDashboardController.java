package com.example.monoauction.admin.controller;

import com.example.monoauction.admin.dto.analytics.*;
import com.example.monoauction.admin.service.AnalyticsService;
import com.example.monoauction.common.dto.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/dashboard")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminDashboardController {

    private final AnalyticsService analyticsService;

    @GetMapping("/overview")
    public ResponseEntity<ApiResponse<DashboardOverview>> getDashboardOverview() {
        DashboardOverview overview = analyticsService.getDashboardOverview();
        return ResponseEntity.ok(ApiResponse.success(overview));
    }

    @GetMapping("/batch/{batchId}")
    public ResponseEntity<ApiResponse<BatchAnalytics>> getBatchAnalytics(@PathVariable Long batchId) {
        BatchAnalytics analytics = analyticsService.getBatchAnalytics(batchId);
        return ResponseEntity.ok(ApiResponse.success(analytics));
    }

    @GetMapping("/categories")
    public ResponseEntity<ApiResponse<List<CategoryAnalytics>>> getCategoryAnalytics() {
        List<CategoryAnalytics> categories = analyticsService.getCategoryAnalytics();
        return ResponseEntity.ok(ApiResponse.success(categories));
    }

    @GetMapping("/top-sellers")
    public ResponseEntity<ApiResponse<List<TopSeller>>> getTopSellers(
            @RequestParam(defaultValue = "10") int limit) {
        List<TopSeller> topSellers = analyticsService.getTopSellers(limit);
        return ResponseEntity.ok(ApiResponse.success(topSellers));
    }

    @GetMapping("/top-buyers")
    public ResponseEntity<ApiResponse<List<TopBuyer>>> getTopBuyers(
            @RequestParam(defaultValue = "10") int limit) {
        List<TopBuyer> topBuyers = analyticsService.getTopBuyers(limit);
        return ResponseEntity.ok(ApiResponse.success(topBuyers));
    }

    @GetMapping("/revenue-trends")
    public ResponseEntity<ApiResponse<List<RevenueTimeSeries>>> getRevenueTrends(
            @RequestParam(defaultValue = "12") int batchCount) {
        List<RevenueTimeSeries> trends = analyticsService.getRevenueTimeSeries(batchCount);
        return ResponseEntity.ok(ApiResponse.success(trends));
    }

}
