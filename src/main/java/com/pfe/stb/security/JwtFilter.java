package com.pfe.stb.security;

import jakarta.annotation.Nonnull;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * JwtFilter is a custom filter that extends OncePerRequestFilter. It intercepts HTTP requests to
 * validate JWT tokens and set the authentication in the security context.
 */
@Component
@RequiredArgsConstructor
@Log4j2
public class JwtFilter extends OncePerRequestFilter {

  private final JwtService jwtService;

  /**
   * Filters incoming HTTP requests to validate JWT tokens. If the token is valid, it sets the
   * authentication in the security context.
   *
   * @param request the HTTP request
   * @param response the HTTP response
   * @param filterChain the filter chain
   * @throws ServletException if an error occurs during filtering
   * @throws IOException if an I/O error occurs during filtering
   */
  @Override
  protected void doFilterInternal(
      @Nonnull HttpServletRequest request,
      @Nonnull HttpServletResponse response,
      @Nonnull FilterChain filterChain)
      throws ServletException, IOException {

    final String authorizationHeader = request.getHeader(HttpHeaders.AUTHORIZATION);
    final String jwt;
    String requestPath = request.getServletPath();

    if (authorizationHeader == null || !authorizationHeader.startsWith("Bearer ")) {
      log.warn(
          "Skipping filtering, Authorization header missing or invalid for request: {}",
          requestPath);
      filterChain.doFilter(request, response);
      return;
    }
    // Extract JWT token from the authorization header
    jwt = authorizationHeader.substring(7);

    String userId = jwtService.extractUsername(jwt);
    var authorities = jwtService.extractAuthorities(jwt);

    // If the user id is not null and the security context does not already have an
    // authentication, proceed with validation
    if (userId != null && SecurityContextHolder.getContext().getAuthentication() == null) {

      // If the token is valid, set the authentication in the security context
      if (jwtService.isTokenValid(jwt, userId) && StringUtils.hasText(jwt)) {
        UsernamePasswordAuthenticationToken authToken =
            new UsernamePasswordAuthenticationToken(userId, null, authorities);
        authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
        SecurityContextHolder.getContext().setAuthentication(authToken);
        log.info("Authentication successful for user: {}", userId);
      } else {
        log.info("Authentication failed for user: {}", userId);
      }
    }
    // Continue with the filter chain
    filterChain.doFilter(request, response);
  }
}
