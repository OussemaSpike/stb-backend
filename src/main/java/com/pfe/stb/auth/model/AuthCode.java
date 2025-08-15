package com.pfe.stb.auth.model;

import com.pfe.stb.auth.model.enums.CodeStatus;
import com.pfe.stb.auth.model.enums.CodeType;
import com.pfe.stb.shared.AbstractAuditingEntity;
import jakarta.persistence.*;
import java.time.Instant;
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
@Table(
    name = "codes",
    indexes = {@Index(name = "idx_user_id_on_codes", columnList = "userId")})
public class AuthCode extends AbstractAuditingEntity {

  @Column(name = "id", updatable = false, unique = true)
  @GeneratedValue
  @Id
  private UUID id;

  @Column(name = "code", nullable = false)
  private String code;

  @Column(name = "expiredAt", nullable = false)
  private Instant expireAt;

  @Column(name = "type", nullable = false)
  @Enumerated(EnumType.STRING)
  private CodeType type;

  @Column(name = "status", nullable = false)
  @Enumerated(EnumType.STRING)
  private CodeStatus status;

  @Column(name = "userId")
  private UUID userId;

  @Column(name = "email")
  private String email;
}
