package com.pfe.stb.config.init;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class Init {

  @Bean
  @Transactional
  public CommandLineRunner run(
      RolesInitializer rolesInitializer, DefaultUserInitializer defaultUserInitializer) {
    return args -> {
      rolesInitializer.createRoles();
      defaultUserInitializer.createDefaultAdmins();
    };
  }
  ;
}
