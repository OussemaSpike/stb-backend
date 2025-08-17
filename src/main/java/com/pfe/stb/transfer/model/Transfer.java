package com.pfe.stb.transfer.model;

import com.pfe.stb.shared.AbstractAuditingEntity;
import com.pfe.stb.user.model.BankAccount;
import com.pfe.stb.user.model.User;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder
@Entity
@Table(name = "transfers")
public class Transfer extends AbstractAuditingEntity {

  @Id @GeneratedValue private UUID id;

  @Column(name = "reference", nullable = false, unique = true, length = 50)
  private String reference;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "from_account_id", nullable = false)
  private BankAccount fromAccount;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "beneficiary_id", nullable = false)
  private Beneficiary beneficiary;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "user_id", nullable = false)
  private User user; // User who initiated the transfer

  @Column(name = "amount", nullable = false, precision = 15, scale = 2)
  private BigDecimal amount;

  @Column(name = "currency", nullable = false, length = 3)
  private String currency = "TND";

  @Column(name = "reason", nullable = false, length = 500)
  private String reason; // Motif du virement (obligatoire)

  @Enumerated(EnumType.STRING)
  @Column(name = "status", nullable = false)
  private TransferStatus status = TransferStatus.PENDING;

  @Column(name = "execution_date")
  private LocalDateTime executionDate;

  @Column(name = "completed_at")
  private LocalDateTime completedAt;

  @Column(name = "failure_reason", length = 500)
  private String failureReason;

  @Column(name = "fees", precision = 15, scale = 2)
  private BigDecimal fees = BigDecimal.ZERO;

  @Column(name = "total_amount", precision = 15, scale = 2)
  private BigDecimal totalAmount; // amount + fees

  @Column(name = "admin_comment", length = 1000)
  private String adminComment;

  @Column(name = "rejection_reason", length = 500)
  private String rejectionReason;

  @Column(name = "approved_by")
  private UUID approvedBy; // Admin who approved the transfer

  @Column(name = "approved_at")
  private LocalDateTime approvedAt;

  @Column(name = "rejected_by")
  private UUID rejectedBy; // Admin who rejected the transfer

  @Column(name = "rejected_at")
  private LocalDateTime rejectedAt;

  @Column(name = "ip_address", length = 45)
  private String ipAddress;

  @PrePersist
  public void calculateTotalAmount() {
    if (amount != null && fees != null) {
      this.totalAmount = amount.add(fees);
    }
  }

  @PreUpdate
  public void updateTotalAmount() {
    calculateTotalAmount();
  }

  // Helper methods
  public boolean isPending() {
    return status == TransferStatus.PENDING;
  }

  public boolean isCompleted() {
    return status == TransferStatus.COMPLETED;
  }

  public boolean isFailed() {
    return status == TransferStatus.FAILED;
  }

  public boolean canBeExecuted() {
    return status == TransferStatus.PENDING;
  }
}
