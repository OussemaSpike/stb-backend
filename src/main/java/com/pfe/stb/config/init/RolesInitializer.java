package com.pfe.stb.config.init;

import static com.pfe.stb.user.model.enums.RoleType.ADMIN;
import static com.pfe.stb.user.model.enums.RoleType.CLIENT;

import com.pfe.stb.user.model.Role;
import com.pfe.stb.user.repository.RoleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
public class RolesInitializer {

  private final RoleRepository rolesRepository;

  public void createRoles() {
    if (rolesRepository.findByName(CLIENT) == null) {
      rolesRepository.save(Role.builder().name(CLIENT).build());
    }
    if (rolesRepository.findByName(ADMIN) == null) {
      rolesRepository.save(Role.builder().name(ADMIN).build());
    }
  }
}
