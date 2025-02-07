package de.caritas.cob.consultingtypeservice.config;

import static java.util.Objects.nonNull;

import com.google.common.collect.Lists;
import de.caritas.cob.consultingtypeservice.api.auth.AuthenticatedUser;
import de.caritas.cob.consultingtypeservice.api.exception.KeycloakException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.Data;
import org.hibernate.validator.constraints.URL;
import org.keycloak.adapters.KeycloakConfigResolver;
import org.keycloak.adapters.springboot.KeycloakSpringBootConfigResolver;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.context.WebApplicationContext;

@Data
@Configuration
@Validated
@ConfigurationProperties(prefix = "keycloak")
public class KeycloakConfig {

  @Bean
  @Scope(scopeName = WebApplicationContext.SCOPE_REQUEST, proxyMode = ScopedProxyMode.TARGET_CLASS)
  AuthenticatedUser authenticatedUser(HttpServletRequest request) {
    var userPrincipal = request.getUserPrincipal();
    var authenticatedUser = new AuthenticatedUser();

    if (nonNull(userPrincipal)) {
      var authToken = (JwtAuthenticationToken) userPrincipal;
      var token = authToken.getToken(); // Extract the JWT token from the authentication token
      var claimMap = token.getClaims(); // Retrieve claims from the JWT token

      try {
        if (claimMap.containsKey("username")) {
          authenticatedUser.setUsername(claimMap.get("username").toString());
        }
        authenticatedUser.setUserId(claimMap.get("userId").toString());
        authenticatedUser.setAccessToken(token.getTokenValue());
        authenticatedUser.setRoles(extractRealmRoles(token).stream().collect(Collectors.toSet()));
      } catch (Exception exception) {
        throw new KeycloakException("Keycloak data missing.", exception);
      }

      var authorities =
          SecurityContextHolder.getContext().getAuthentication().getAuthorities().stream()
              .map(Object::toString)
              .collect(Collectors.toSet());
      authenticatedUser.setGrantedAuthorities(authorities);
    }

    return authenticatedUser;
  }

  public Collection<String> extractRealmRoles(Jwt jwt) {
    Map<String, Object> realmAccess = (Map<String, Object>) jwt.getClaims().get("realm_access");
    if (realmAccess != null) {
      var roles = (List<String>) realmAccess.get("roles");
      if (roles != null) {
        return roles;
      }
    }
    return Lists.newArrayList();
  }

  @Bean
  public KeycloakConfigResolver keycloakConfigResolver() {
    return new KeycloakSpringBootConfigResolver();
  }

  @URL private String authServerUrl;

  @NotBlank private String realm;

  @NotBlank private String resource;

  @NotBlank private String principalAttribute;

  @NotNull private Boolean cors;
}
