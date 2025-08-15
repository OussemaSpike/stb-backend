package com.pfe.stb.config.security;

import com.pfe.stb.security.JwtFilter;
import com.pfe.stb.security.RequestLoggingFilter;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
@EnableMethodSecurity
@Log4j2
public class SecurityConfig {

  private final ApiEndpointSecurityInspector apiEndpointSecurityInspector;
  private final JwtFilter jwtAuthFilter;
  private final AuthenticationProvider authenticationProvider;

  @Value("${application.cors.allowed-origins:*}")
  private List<String> allowedOrigins;

  @Bean
  public SecurityFilterChain jwtAuthorizationSecurityFilterChain(HttpSecurity http)
      throws Exception {
    return http.cors(Customizer.withDefaults())
        .csrf(
            AbstractHttpConfigurer
                ::disable) // we don't need CSRF protection for JWT based authentication
        .sessionManagement(
            session ->
                session.sessionCreationPolicy(
                    SessionCreationPolicy.STATELESS)) // JWT doesn't need sessions
        .authorizeHttpRequests(
            request ->
                request
                    .requestMatchers(
                        HttpMethod.GET,
                        apiEndpointSecurityInspector.getPublicGetEndpoints().toArray(String[]::new))
                    .permitAll()
                    .requestMatchers(
                        HttpMethod.POST,
                        apiEndpointSecurityInspector
                            .getPublicPostEndpoints()
                            .toArray(String[]::new))
                    .permitAll()
                    .requestMatchers(
                        HttpMethod.PUT,
                        apiEndpointSecurityInspector.getPublicPutEndpoints().toArray(String[]::new))
                    .permitAll()
                    .anyRequest()
                    .permitAll())
        .authenticationProvider(authenticationProvider)
        // Add the JWT filter before the UsernamePasswordAuthenticationFilter
        .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class)
        .addFilterAfter(new RequestLoggingFilter(), UsernamePasswordAuthenticationFilter.class)
        .build();
  }

  //  @Bean
  //  public CommonsRequestLoggingFilter requestLoggingFilter() {
  //    CommonsRequestLoggingFilter loggingFilter = new CommonsRequestLoggingFilter();
  //    loggingFilter.setIncludeClientInfo(true);
  //    loggingFilter.setIncludeQueryString(true);
  //    loggingFilter.setIncludePayload(true);
  //    loggingFilter.setMaxPayloadLength(10000);
  //    loggingFilter.setIncludeHeaders(false);
  //    return loggingFilter;
  //  }

  @Bean
  public CorsFilter corsFilter() {
    log.info("Configuring CORS filter with allowed origins: {}", allowedOrigins);
    CorsConfiguration config = new CorsConfiguration();
    config.setAllowCredentials(true);
    config.setAllowedOrigins(allowedOrigins);
    config.setAllowedHeaders(List.of("*"));
    config.setAllowedMethods(List.of("*"));
    UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
    source.registerCorsConfiguration("/**", config);
    return new CorsFilter(source);
  }
}
