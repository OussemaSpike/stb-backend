package com.pfe.stb.auth.utils;

import com.pfe.stb.user.model.User;
import java.util.Map;

public class TokenUtils {

  static final String NAME = "name";
  static final String FIRST_NAME = "first_name";
  static final String LAST_NAME = "last_name";
  static final String EMAIL = "email";
  static final String EMAIL_VERIFIED = "email_verified";
  static final String TYP = "typ";
  static final String BEARER = "Bearer";
  static final String REFRESH = "Refresh";

  private TokenUtils() {
    throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
  }

  public static Map<String, Object> createClaims(User user) {

    return Map.of(
        NAME, user.getFullName(),
        FIRST_NAME, user.getFirstName(),
        LAST_NAME, user.getLastName(),
        EMAIL, user.getEmail(),
        EMAIL_VERIFIED, user.isEmailVerified(),
        TYP, BEARER);
  }

  public static Map<String, Object> createRefreshClaims(User user) {

    return Map.of(EMAIL, user.getEmail(), TYP, REFRESH);
  }
}
