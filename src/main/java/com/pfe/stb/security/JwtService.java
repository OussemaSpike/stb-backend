package com.pfe.stb.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import java.security.Key;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

@Getter
@Service
@RequiredArgsConstructor
@EnableConfigurationProperties(JwtProperties.class)
public class JwtService {

  static final String TYP = "typ";
  static final String ISSUER = "stb";
  static final String AUTHORITIES = "authorities";
  private final JwtProperties jwtProperties;

  /**
   * Extracts the username from the JWT token.
   *
   * @param token the JWT token
   * @return the username extracted from the token
   */
  public String extractUsername(String token) {
    return extractClaim(token, Claims::getSubject);
  }

  public List<SimpleGrantedAuthority> extractAuthorities(String token) {
    return extractClaim(
        token,
        claims -> {
          List<?> authorities = claims.get(AUTHORITIES, List.class);
          List<String> authorityStrings = new ArrayList<>();
          for (Object authority : authorities) {
            if (authority instanceof String) {
              authorityStrings.add("ROLE_" + authority);
            }
          }
          return authorityStrings.stream().map(SimpleGrantedAuthority::new).toList();
        });
  }

  /**
   * Extracts a specific claim from the JWT token.
   *
   * @param token the JWT token
   * @param claimsResolver a function to resolve the claim
   * @param <T> the type of the claim
   * @return the claim extracted from the token
   */
  public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
    final Claims claims = extractAllClaims(token);
    return claimsResolver.apply(claims);
  }

  /**
   * Extracts all claims from the JWT token.
   *
   * @param token the JWT token
   * @return the claims extracted from the token
   */
  public Claims extractAllClaims(String token) {

    return Jwts.parserBuilder()
        .setSigningKey(getSignInKey())
        .build()
        .parseClaimsJws(token)
        .getBody();
  }

  /**
   * Generates a JWT token with the given claims and user details.
   *
   * @param claims the claims to include in the token
   * @param userDetails the user details
   * @return the generated JWT token
   */
  public String generateToken(Map<String, Object> claims, UserDetails userDetails) {
    return buildToken(claims, userDetails, jwtProperties.getTokenExpiration());
  }

  /**
   * Generates a refresh token for the given user details.
   *
   * @param userDetails the user details
   * @return the generated refresh token
   */
  public String generateRefreshToken(Map<String, Object> claims, UserDetails userDetails) {
    long validity = jwtProperties.getRefreshTokenExpiration();
    return buildToken(claims, userDetails, validity);
  }

  /**
   * Builds a JWT token with the given claims, user details, and expiration time.
   *
   * @param extraClaims the extra claims to include in the token
   * @param userDetails the user details
   * @param jwtExpiration the expiration time of the token
   * @return the built JWT token
   */
  private String buildToken(
      Map<String, Object> extraClaims, UserDetails userDetails, long jwtExpiration) {
    var authorities =
        userDetails.getAuthorities().stream().map(GrantedAuthority::getAuthority).toList();

    return Jwts.builder()
        .setClaims(extraClaims)
        .setHeaderParam(TYP, "JWT")
        .setSubject(userDetails.getUsername())
        .setIssuedAt(new Date(System.currentTimeMillis()))
        .setExpiration(new Date(System.currentTimeMillis() + jwtExpiration))
        .setIssuer(ISSUER)
        .claim(AUTHORITIES, authorities)
        .signWith(getSignInKey())
        .compact();
  }

  /**
   * Validates the JWT token against the given user details.
   *
   * @param token the JWT token
   * @return true if the token is valid, false otherwise
   */
  public boolean isTokenValid(String token, String subject) {
    var type = extractClaim(token, claims -> claims.get("typ", String.class));
    final String username = extractUsername(token);
    return username.equals(subject) && isTokenNonExpired(token) && type.equals("Bearer");
  }

  public boolean isRefreshTokenValid(String token, String subject) {
    final String username = extractUsername(token);
    return username.equals(subject) && isTokenNonExpired(token) && extractIsRefreshToken(token);
  }

  /**
   * Checks if the JWT token is expired.
   *
   * @param token the JWT token
   * @return true if the token is expired, false otherwise
   */
  private boolean isTokenNonExpired(String token) {
    return !extractExpiration(token).before(new Date());
  }

  /**
   * Extracts the expiration date from the JWT token.
   *
   * @param token the JWT token
   * @return the expiration date extracted from the token
   */
  private Date extractExpiration(String token) {
    return extractClaim(token, Claims::getExpiration);
  }

  /**
   * Gets the signing key used to sign the JWT token.
   *
   * @return the signing key
   */
  private Key getSignInKey() {
    byte[] keyBytes = Decoders.BASE64.decode(jwtProperties.getSecretKey());
    return Keys.hmacShaKeyFor(keyBytes);
  }

  public boolean extractIsRefreshToken(String refreshToken) {
    var type = extractClaim(refreshToken, claims -> claims.get("typ", String.class));
    return type.equals("Refresh");
  }
}
