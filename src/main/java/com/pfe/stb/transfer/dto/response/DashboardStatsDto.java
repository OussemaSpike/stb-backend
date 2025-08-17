package com.pfe.stb.transfer.dto.response;

import java.math.BigDecimal;
import java.time.LocalDate;

public record DashboardStatsDto(
    // General statistics
    Long totalUsers,
    Long activeUsers,
    Long inactiveUsers,
    
    // Transfer statistics
    Long totalTransfers,
    Long pendingTransfers,
    Long completedTransfers,
    Long rejectedTransfers,
    Long failedTransfers,
    
    // Amount statistics
    BigDecimal totalAmountTransferred,
    BigDecimal totalAmountPending,
    BigDecimal averageTransferAmount,
    
    // Today's statistics
    Long todayTransfers,
    BigDecimal todayAmount,
    
    // This month statistics
    Long monthTransfers,
    BigDecimal monthAmount,
    
    // Performance metrics
    Double approvalRate,
    Double rejectionRate,
    Double failureRate
) {}
