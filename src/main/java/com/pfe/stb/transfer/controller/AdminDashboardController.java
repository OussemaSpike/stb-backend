package com.pfe.stb.transfer.controller;

import com.pfe.stb.transfer.dto.response.*;
import com.pfe.stb.transfer.service.DashboardService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/admin/dashboard")
@Tag(name = "Admin Dashboard", description = "Dashboard statistics and charts for administrators")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
@PreAuthorize("hasRole('ADMIN')")
public class AdminDashboardController {

    private final DashboardService dashboardService;

    @Operation(
        summary = "Get dashboard statistics",
        description = "Retrieve comprehensive dashboard statistics including user counts, transfer metrics, and performance indicators."
    )
    @GetMapping("/stats")
    public ResponseEntity<DashboardStatsDto> getDashboardStats() {
        DashboardStatsDto stats = dashboardService.getDashboardStats();
        return ResponseEntity.ok(stats);
    }

    @Operation(
        summary = "Get transfer chart data",
        description = "Retrieve transfer data for charts showing trends over the specified number of days."
    )
    @GetMapping("/charts/transfers")
    public ResponseEntity<List<TransferChartDataDto>> getTransferChartData(
        @Parameter(description = "Number of days to include in the chart (default: 30)")
        @RequestParam(defaultValue = "30") int days
    ) {
        if (days < 1) days = 30;
        if (days > 365) days = 365; // Limit to 1 year
        
        List<TransferChartDataDto> chartData = dashboardService.getTransferChartData(days);
        return ResponseEntity.ok(chartData);
    }

    @Operation(
        summary = "Get transfer status statistics",
        description = "Retrieve transfer status distribution for pie charts showing the percentage of each status."
    )
    @GetMapping("/charts/transfer-status")
    public ResponseEntity<List<TransferStatusStatsDto>> getTransferStatusStats() {
        List<TransferStatusStatsDto> statusStats = dashboardService.getTransferStatusStats();
        return ResponseEntity.ok(statusStats);
    }

    @Operation(
        summary = "Get top beneficiaries",
        description = "Retrieve the most frequently used beneficiaries with their transfer counts and amounts."
    )
    @GetMapping("/charts/top-beneficiaries")
    public ResponseEntity<List<TopBeneficiaryDto>> getTopBeneficiaries(
        @Parameter(description = "Number of top beneficiaries to return (default: 10)")
        @RequestParam(defaultValue = "10") int limit
    ) {
        if (limit < 1) limit = 10;
        if (limit > 50) limit = 50; // Reasonable limit
        
        List<TopBeneficiaryDto> topBeneficiaries = dashboardService.getTopBeneficiaries(limit);
        return ResponseEntity.ok(topBeneficiaries);
    }

    @Operation(
        summary = "Get monthly comparison data",
        description = "Compare current month performance with the previous month including growth percentages."
    )
    @GetMapping("/comparison/monthly")
    public ResponseEntity<MonthlyComparisonDto> getMonthlyComparison() {
        MonthlyComparisonDto comparison = dashboardService.getMonthlyComparison();
        return ResponseEntity.ok(comparison);
    }

    @Operation(
        summary = "Get weekly transfer trends",
        description = "Get transfer data for the last 7 days for weekly trend analysis."
    )
    @GetMapping("/charts/weekly-trend")
    public ResponseEntity<List<TransferChartDataDto>> getWeeklyTrend() {
        List<TransferChartDataDto> weeklyData = dashboardService.getTransferChartData(7);
        return ResponseEntity.ok(weeklyData);
    }

    @Operation(
        summary = "Get yearly overview",
        description = "Get transfer data for the entire year for annual analysis."
    )
    @GetMapping("/charts/yearly-overview")
    public ResponseEntity<List<TransferChartDataDto>> getYearlyOverview() {
        List<TransferChartDataDto> yearlyData = dashboardService.getTransferChartData(365);
        return ResponseEntity.ok(yearlyData);
    }
}
