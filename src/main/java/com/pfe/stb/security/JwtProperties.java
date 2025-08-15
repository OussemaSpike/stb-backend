package com.pfe.stb.security;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "application.security.jwt")
@Getter
@Setter
public class JwtProperties {
  private String secretKey;
  private long tokenExpiration;
  private long refreshTokenExpiration;
}
