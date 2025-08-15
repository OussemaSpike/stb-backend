package com.pfe.stb.user.repository;

import com.pfe.stb.user.model.enums.RoleType;
import com.pfe.stb.user.model.Role;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RoleRepository extends JpaRepository<Role, UUID> {
  Role findByName(RoleType name);
}
