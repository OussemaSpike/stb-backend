package com.pfe.stb.shared.security;


import com.pfe.stb.exception.UnauthorizedException;
import java.util.*;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

public class AuthUtils { 


  private AuthUtils() {
    throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
  }

  public static UUID getCurrentAuthenticatedUserId() {
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    if (!isAuthenticated()) {
      throw new UnauthorizedException(
          UnauthorizedException.UnauthorizedExceptionType.USER_NOT_AUTHORIZED);
    }
    return UUID.fromString(authentication.getName());
  }

  public static UUID getCurrentUserId() {
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    if (authentication == null
        || !authentication.isAuthenticated()
        || authentication instanceof AnonymousAuthenticationToken) {
      return null;
    }
    return UUID.fromString(authentication.getName());
  }

  public static List<String> getAuthorities() {
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    if (authentication == null || !authentication.isAuthenticated()) {
      return List.of();
    }

    return authentication.getAuthorities().stream().map(GrantedAuthority::getAuthority).toList();
  }

  public static boolean isAuthenticated() {
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    return authentication != null
        && authentication.isAuthenticated()
        && !(authentication instanceof AnonymousAuthenticationToken);
  }
}
