package com.pfe.stb.transfer.service;

import com.pfe.stb.transfer.dto.response.*;
import com.pfe.stb.transfer.model.TransferStatus;
import com.pfe.stb.transfer.repository.TransferRepository;
import com.pfe.stb.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Log4j2
public class DashboardService {

    private final TransferRepository transferRepository;
    private final UserRepository userRepository;

    /**
     * Get comprehensive dashboard statistics
     */
    public DashboardStatsDto getDashboardStats() {
        log.info("Generating dashboard statistics");

        // User statistics
        Long totalUsers = userRepository.count();
        Long activeUsers = userRepository.countByEnabledTrue();
        Long inactiveUsers = totalUsers - activeUsers;

        // Transfer counts by status
        Long totalTransfers = transferRepository.count();
        Long pendingTransfers = transferRepository.countByStatus(TransferStatus.PENDING);
        Long completedTransfers = transferRepository.countByStatus(TransferStatus.COMPLETED);
        Long rejectedTransfers = transferRepository.countByStatus(TransferStatus.CANCELLED);
        Long failedTransfers = transferRepository.countByStatus(TransferStatus.FAILED);

        // Amount statistics
        BigDecimal totalAmountTransferred = transferRepository.getTotalAmountTransferred();
        if (totalAmountTransferred == null) totalAmountTransferred = BigDecimal.ZERO;

        BigDecimal totalAmountPending = transferRepository.sumAmountByStatus(TransferStatus.PENDING);
        if (totalAmountPending == null) totalAmountPending = BigDecimal.ZERO;

        BigDecimal averageTransferAmount = transferRepository.getAverageTransferAmount(TransferStatus.COMPLETED);
        if (averageTransferAmount == null) averageTransferAmount = BigDecimal.ZERO;

        // Today's statistics
        Long todayTransfers = transferRepository.getTodayTransfersCount();
        BigDecimal todayAmount = transferRepository.getTodayTransfersAmount();
        if (todayAmount == null) todayAmount = BigDecimal.ZERO;

        // This month statistics
        Long monthTransfers = transferRepository.getMonthTransfersCount();
        BigDecimal monthAmount = transferRepository.getMonthTransfersAmount();
        if (monthAmount == null) monthAmount = BigDecimal.ZERO;

        // Performance metrics (as percentages)
        Double approvalRate = calculatePercentage(completedTransfers, totalTransfers);
        Double rejectionRate = calculatePercentage(rejectedTransfers, totalTransfers);
        Double failureRate = calculatePercentage(failedTransfers, totalTransfers);

        return new DashboardStatsDto(
            totalUsers,
            activeUsers,
            inactiveUsers,
            totalTransfers,
            pendingTransfers,
            completedTransfers,
            rejectedTransfers,
            failedTransfers,
            totalAmountTransferred,
            totalAmountPending,
            averageTransferAmount,
            todayTransfers,
            todayAmount,
            monthTransfers,
            monthAmount,
            approvalRate,
            rejectionRate,
            failureRate
        );
    }

    /**
     * Get transfer chart data for the last N days
     */
    public List<TransferChartDataDto> getTransferChartData(int days) {
        log.info("Generating transfer chart data for last {} days", days);
        
        LocalDate endDate = LocalDate.now();
        LocalDate startDate = endDate.minusDays(days - 1);

        List<Object[]> chartData = transferRepository.getTransferChartData(startDate, endDate);

        return chartData.stream()
            .map(row -> {
                // Handle different data types from native query
                LocalDate date;
                if (row[0] instanceof java.sql.Date) {
                    date = ((java.sql.Date) row[0]).toLocalDate();
                } else {
                    date = (LocalDate) row[0];
                }
                
                Long count = ((Number) row[1]).longValue();
                BigDecimal amount = row[2] != null ? new BigDecimal(row[2].toString()) : BigDecimal.ZERO;
                
                return new TransferChartDataDto(date, count, amount);
            })
            .collect(Collectors.toList());
    }

    /**
     * Get transfer status statistics for pie chart
     */
    public List<TransferStatusStatsDto> getTransferStatusStats() {
        log.info("Generating transfer status statistics");
        
        List<Object[]> statusStats = transferRepository.getTransferStatusStats();
        Long totalTransfers = transferRepository.count();
        
        return statusStats.stream()
            .map(row -> {
                TransferStatus status = (TransferStatus) row[0];
                Long count = ((Number) row[1]).longValue();
                Double percentage = calculatePercentage(count, totalTransfers);
                
                return new TransferStatusStatsDto(status, count, percentage);
            })
            .collect(Collectors.toList());
    }

    /**
     * Get top beneficiaries by transfer count
     */
    public List<TopBeneficiaryDto> getTopBeneficiaries(int limit) {
        log.info("Generating top {} beneficiaries statistics", limit);
        
        List<Object[]> topBeneficiaries = transferRepository.getTopBeneficiaries(limit);

        return topBeneficiaries.stream()
            .limit(limit)
            .map(row -> new TopBeneficiaryDto(
                (String) row[0],
                (String) row[1],
                ((Number) row[2]).longValue(),
                row[3] != null ? new BigDecimal(row[3].toString()) : BigDecimal.ZERO
            ))
            .collect(Collectors.toList());
    }

    /**
     * Get monthly comparison data (current vs previous month)
     */
    public MonthlyComparisonDto getMonthlyComparison() {
        log.info("Generating monthly comparison data");
        
        LocalDate now = LocalDate.now();
        
        // Current month data
        Long currentMonthTransfers = transferRepository.getMonthTransfersCount();
        BigDecimal currentMonthAmount = transferRepository.getMonthTransfersAmount();
        
        // Previous month data - would need additional queries
        // For now, we'll create placeholder data
        Long previousMonthTransfers = 0L; // Would implement with additional query
        BigDecimal previousMonthAmount = BigDecimal.ZERO; // Would implement with additional query
        
        Double transferGrowth = calculateGrowthPercentage(previousMonthTransfers, currentMonthTransfers);
        Double amountGrowth = calculateGrowthPercentage(previousMonthAmount, currentMonthAmount);
        
        return new MonthlyComparisonDto(
            currentMonthTransfers,
            currentMonthAmount,
            previousMonthTransfers,
            previousMonthAmount,
            transferGrowth,
            amountGrowth
        );
    }

    /**
     * Helper method to calculate percentage
     */
    private Double calculatePercentage(Long numerator, Long denominator) {
        if (denominator == 0) return 0.0;
        return (numerator.doubleValue() / denominator.doubleValue()) * 100.0;
    }

    /**
     * Helper method to calculate growth percentage
     */
    private Double calculateGrowthPercentage(Long previous, Long current) {
        if (previous == 0) return current > 0 ? 100.0 : 0.0;
        return ((current.doubleValue() - previous.doubleValue()) / previous.doubleValue()) * 100.0;
    }

    /**
     * Helper method to calculate growth percentage for BigDecimal
     */
    private Double calculateGrowthPercentage(BigDecimal previous, BigDecimal current) {
        if (previous.compareTo(BigDecimal.ZERO) == 0) {
            return current.compareTo(BigDecimal.ZERO) > 0 ? 100.0 : 0.0;
        }
        return current.subtract(previous)
                .divide(previous, 4, RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(100))
                .doubleValue();
    }
}
