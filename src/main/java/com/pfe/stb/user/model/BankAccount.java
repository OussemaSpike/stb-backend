package com.pfe.stb.user.model;

import com.pfe.stb.shared.AbstractAuditingEntity;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.util.UUID;
import lombok.*;
import lombok.experimental.SuperBuilder;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder
@Entity
@Table(name = "bank_accounts")
public class BankAccount extends AbstractAuditingEntity {

  // --- Identification (Core) ---
  @Id @GeneratedValue private UUID id;

  @Column(name = "rib", nullable = false, unique = true, length = 20)
  private String rib;

  @Enumerated(EnumType.STRING)
  @Column(name = "account_type", nullable = false)
  private BankAccountType accountType; // e.g., COURANT, EPARGNE

  // --- User Relationship (Core) ---
  @OneToOne(fetch = FetchType.LAZY) // Or @ManyToOne if user can have multiple accounts
  @JoinColumn(name = "user_id", nullable = false)
  private User user;

  // --- Financials (Core) ---
  @Column(name = "balance", nullable = false, precision = 15, scale = 3)
  private BigDecimal balance = BigDecimal.ZERO;

  @Column(name = "available_balance", nullable = false, precision = 15, scale = 3)
  private BigDecimal availableBalance = BigDecimal.ZERO;

  @Column(name = "blocked_amount", precision = 15, scale = 3)
  private BigDecimal blockedAmount = BigDecimal.ZERO;

  @Column(name = "currency", nullable = false, length = 3)
  private String currency = "TND";

  @Enumerated(EnumType.STRING)
  @Column(name = "status", nullable = false)
  private AccountStatus status = AccountStatus.ACTIVE;

  @Column(name = "block_reason")
  private String blockReason;

  /** JPA callback to ensure consistency of balances and limits before saving. */
  @PrePersist
  @PreUpdate
  public void updateDerivedFields() {
    // Calculate available balance
    if (this.balance != null && this.blockedAmount != null) {
      this.availableBalance = this.balance.subtract(this.blockedAmount);
    }
  }

  // Business logic methods
  public boolean canTransfer(BigDecimal amount) {
    if (this.status != AccountStatus.ACTIVE) {
      return false;
    }

    // Check if amount is within available balance
    return this.availableBalance.compareTo(amount) >= 0;
  }

  public boolean hasSufficientBalance(BigDecimal amount) {
    return this.availableBalance.compareTo(amount) >= 0;
  }
}
