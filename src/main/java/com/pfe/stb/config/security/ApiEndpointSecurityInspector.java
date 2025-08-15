package com.pfe.stb.config.security;

import static com.pfe.stb.config.Constants.SKIPPED_PATHS;
import static org.springframework.http.HttpMethod.*;

import io.swagger.v3.oas.models.PathItem.HttpMethod;
import jakarta.annotation.PostConstruct;
import jakarta.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

/**
 * Utility class responsible for evaluating the accessibility of API endpoints based on their
 * security configuration. It works in conjunction with the mappings of controller methods annotated
 * with {@link PublicEndpoint}.
 *
 * @see PublicEndpoint
 */
@Component
@RequiredArgsConstructor
public class ApiEndpointSecurityInspector {

  private final RequestMappingHandlerMapping requestMappingHandlerMapping;

  @Getter private final List<String> publicGetEndpoints = new ArrayList<>();
  @Getter private final List<String> publicPostEndpoints = new ArrayList<>();
  @Getter private final List<String> publicPutEndpoints = new ArrayList<>();

  /**
   * Initializes the class by gathering public endpoints for various HTTP methods. It identifies
   * designated public endpoints within the application's mappings and adds them to separate lists
   * based on their associated HTTP methods. If OpenAPI is enabled, Swagger endpoints are also
   * considered as public.
   */
  @PostConstruct
  public void init() {
    final var handlerMethods = requestMappingHandlerMapping.getHandlerMethods();
    handlerMethods.forEach(
        (requestInfo, handlerMethod) -> {
          if (handlerMethod.hasMethodAnnotation(PublicEndpoint.class)) {
            final var httpMethod =
                requestInfo.getMethodsCondition().getMethods().iterator().next().asHttpMethod();
            final var apiPaths = requestInfo.getPathPatternsCondition().getPatternValues();

            if (httpMethod.equals(GET)) {
              publicGetEndpoints.addAll(apiPaths);
            } else if (httpMethod.equals(POST)) {
              publicPostEndpoints.addAll(apiPaths);
            } else if (httpMethod.equals(PUT)) {
              publicPutEndpoints.addAll(apiPaths);
            }
          }
        });

      publicGetEndpoints.addAll(SKIPPED_PATHS);
    
  }

  /**
   * Checks if the provided HTTP request is directed towards an unsecured API endpoint.
   *
   * @param request The HTTP request to inspect.
   * @return {@code true} if the request is to an unsecured API endpoint, {@code false} otherwise.
   */
  public boolean isUnsecureRequest(@NonNull final HttpServletRequest request) {
    final var requestHttpMethod = HttpMethod.valueOf(request.getMethod());
    var unsecuredApiPaths = getUnsecuredApiPaths(requestHttpMethod);
    unsecuredApiPaths = Optional.ofNullable(unsecuredApiPaths).orElseGet(ArrayList::new);

    return unsecuredApiPaths.stream()
        .anyMatch(apiPath -> new AntPathMatcher().match(apiPath, request.getRequestURI()));
  }

  /**
   * Retrieves the list of unsecured API paths based on the provided HTTP method.
   *
   * @param httpMethod The HTTP method for which unsecured paths are to be retrieved.
   * @return A list of unsecured API paths for the specified HTTP method.s
   */
  private List<String> getUnsecuredApiPaths(@NonNull final HttpMethod httpMethod) {
    return switch (httpMethod) {
      case GET -> publicGetEndpoints;
      case POST -> publicPostEndpoints;
      case PUT -> publicPutEndpoints;
      default -> Collections.emptyList();
    };
  }
}
