package com.pfe.stb.transfer.repository;

import com.pfe.stb.transfer.model.Transfer;
import com.pfe.stb.transfer.model.TransferStatus;
import com.pfe.stb.user.model.User;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface TransferRepository
    extends JpaRepository<Transfer, UUID>, JpaSpecificationExecutor<Transfer> {

  @Query(
      "SELECT SUM(t.amount) FROM Transfer t WHERE t.user = :user AND t.status IN :statuses AND DATE(t.createdAt) = :date")
  BigDecimal getDailyTransferAmount(
      @Param("user") User user,
      @Param("statuses") List<TransferStatus> statuses,
      @Param("date") LocalDate date);

  @Query(
      "SELECT SUM(t.amount) FROM Transfer t WHERE t.user = :user AND t.status IN :statuses AND MONTH(t.createdAt) = :month AND YEAR(t.createdAt) = :year")
  BigDecimal getMonthlyTransferAmount(
      @Param("user") User user,
      @Param("statuses") List<TransferStatus> statuses,
      @Param("month") int month,
      @Param("year") int year);

  List<Transfer> findByStatus(TransferStatus status);

  // Dashboard statistics queries
  
  @Query("SELECT COUNT(t) FROM Transfer t WHERE t.status = :status")
  Long countByStatus(@Param("status") TransferStatus status);
  
  @Query("SELECT SUM(t.amount) FROM Transfer t WHERE t.status = :status")
  BigDecimal sumAmountByStatus(@Param("status") TransferStatus status);
  
  @Query("SELECT SUM(t.amount) FROM Transfer t")
  BigDecimal getTotalAmountTransferred();
  
  @Query("SELECT AVG(t.amount) FROM Transfer t WHERE t.status = :status")
  BigDecimal getAverageTransferAmount(@Param("status") TransferStatus status);
  
  @Query(value = "SELECT COUNT(*) FROM transfers WHERE DATE(created_at) = CURRENT_DATE", nativeQuery = true)
  Long getTodayTransfersCount();
  
  @Query(value = "SELECT COALESCE(SUM(amount), 0) FROM transfers WHERE DATE(created_at) = CURRENT_DATE AND status = 'COMPLETED'", nativeQuery = true)
  BigDecimal getTodayTransfersAmount();
  
  @Query(value = "SELECT COUNT(*) FROM transfers WHERE EXTRACT(MONTH FROM created_at) = EXTRACT(MONTH FROM CURRENT_DATE) AND EXTRACT(YEAR FROM created_at) = EXTRACT(YEAR FROM CURRENT_DATE)", nativeQuery = true)
  Long getMonthTransfersCount();
  
  @Query(value = "SELECT COALESCE(SUM(amount), 0) FROM transfers WHERE EXTRACT(MONTH FROM created_at) = EXTRACT(MONTH FROM CURRENT_DATE) AND EXTRACT(YEAR FROM created_at) = EXTRACT(YEAR FROM CURRENT_DATE) AND status = 'COMPLETED'", nativeQuery = true)
  BigDecimal getMonthTransfersAmount();
  
  // Chart data queries
  @Query(value = "SELECT DATE(t.created_at) as date, COUNT(t.id) as count, COALESCE(SUM(t.amount), 0) as amount " +
         "FROM transfers t WHERE DATE(t.created_at) >= :startDate AND DATE(t.created_at) <= :endDate " +
         "GROUP BY DATE(t.created_at) ORDER BY DATE(t.created_at)", nativeQuery = true)
  List<Object[]> getTransferChartData(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);
  
  @Query("SELECT t.status, COUNT(t) " +
         "FROM Transfer t GROUP BY t.status")
  List<Object[]> getTransferStatusStats();
  
  @Query(value = "SELECT b.name, b.rib, COUNT(t.id), SUM(t.amount) " +
         "FROM transfers t JOIN beneficiaries b ON t.beneficiary_id = b.id " +
         "WHERE t.status = 'COMPLETED' " +
         "GROUP BY b.id, b.name, b.rib " +
         "ORDER BY COUNT(t.id) DESC " +
         "LIMIT :limit", nativeQuery = true)
  List<Object[]> getTopBeneficiaries(@Param("limit") int limit);
}
