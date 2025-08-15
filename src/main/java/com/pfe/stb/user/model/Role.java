package com.pfe.stb.user.model;

import com.pfe.stb.user.model.enums.RoleType;
import jakarta.persistence.*;
import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity
@Table(name = "roles")
public class Role {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Integer id;

  @Column(name = "name", nullable = false, unique = true)
  @Enumerated(EnumType.STRING)
  private RoleType name;
}
