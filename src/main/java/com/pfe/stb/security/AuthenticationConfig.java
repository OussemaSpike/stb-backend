package com.pfe.stb.security;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

/** Configuration class for Spring Security authentication. */
@RequiredArgsConstructor
@Configuration
public class AuthenticationConfig {

  private final UserDetailsService userDetailsService;

  /**
   * Bean for password encoding. Uses BCryptPasswordEncoder for secure password storage.
   *
   * @return a PasswordEncoder instance
   */
  @Bean
  public PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder();
  }

  /**
   * Bean for the AuthenticationProvider. Configures DaoAuthenticationProvider with user details
   * service and password encoder.
   *
   * @return an AuthenticationProvider instance
   */
  @Bean
  public AuthenticationProvider authenticationProvider() {
    DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider(userDetailsService);
    authProvider.setPasswordEncoder(passwordEncoder());
    return authProvider;
  }

  /**
   * Bean for AuthenticationManager. Retrieves the AuthenticationManager from the
   * AuthenticationConfiguration.
   *
   * @param config the AuthenticationConfiguration instance
   * @return an AuthenticationManager instance
   * @throws Exception if an error occurs during AuthenticationManager creation
   */
  @Bean
  public AuthenticationManager authenticationManager(AuthenticationConfiguration config)
      throws Exception {
    return config.getAuthenticationManager();
  }
}
