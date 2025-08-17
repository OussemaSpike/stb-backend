package com.pfe.stb.transfer.model;

import com.pfe.stb.shared.AbstractAuditingEntity;
import com.pfe.stb.user.model.User;
import jakarta.persistence.*;
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
@Table(name = "beneficiaries")
public class Beneficiary extends AbstractAuditingEntity {

  @Id @GeneratedValue private UUID id;

  @Column(name = "name", nullable = false, length = 100)
  private String name;

  @Column(name = "rib", nullable = false, length = 20)
  private String rib;

  @Column(name = "is_verified", nullable = false)
  private Boolean isVerified = false;

  @Column(name = "is_active", nullable = false)
  private Boolean isActive = true;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "user_id", nullable = false)
  private User user;
}
