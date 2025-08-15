package com.pfe.stb.config.init;

import com.pfe.stb.user.model.Role;
import com.pfe.stb.user.model.User;
import com.pfe.stb.user.model.enums.RoleType;
import com.pfe.stb.user.repository.RoleRepository;
import com.pfe.stb.user.repository.UserRepository;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
public class DefaultUserInitializer {

  private final UserRepository userRepository;
  private final RoleRepository roleRepository;
  private final PasswordEncoder passwordEncoder;

  public void createDefaultAdmins() {
    createAdmin();
  }

  private void createAdmin() {
    if (userRepository.findByEmail("stb@admin.com").isPresent()) {
      return;
    }

    Role roles = roleRepository.findByName(RoleType.ADMIN);

    User user =
        User.builder()
            .email("admin@stb.com")
            .password(passwordEncoder.encode("admin"))
            .firstName("stb")
            .lastName("stb")
            .emailVerified(true)
            .roles(Set.of(roles))
            .enabled(true)
            .accountLocked(false)
            .phoneNumber("54 123 123")
            .build();
    userRepository.save(user);
  }
}
