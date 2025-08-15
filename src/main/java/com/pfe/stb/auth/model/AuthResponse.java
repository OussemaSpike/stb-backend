package com.pfe.stb.auth.model;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class AuthResponse {

  protected String accessToken;
  protected String refreshToken;
}
