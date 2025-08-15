package com.pfe.stb.config;

import java.util.List;

public class Constants {

  public static final List<String> SKIPPED_PATHS =
      List.of(
          "/swagger-ui",
          "/swagger-ui/**",
          "/v3/api-docs/**",
          "/api-docs/**",
          "/swagger-resources/**",
          "/configuration/ui",
          "/configuration/security",
          "/docs/**",
          "/swagger-ui.html",
          "/webjars/**",
          "/docs.html",
          "/swagger-ui/index.html",
          "/favicon.ico",
          "/actuator/**");

  private Constants() {}
}
