package de.caritas.cob.consultingtypeservice.config;

import static org.springframework.security.config.Customizer.withDefaults;

import de.caritas.cob.consultingtypeservice.config.security.AuthorisationService;
import de.caritas.cob.consultingtypeservice.config.security.JwtAuthConverter;
import de.caritas.cob.consultingtypeservice.config.security.JwtAuthConverterProperties;
import de.caritas.cob.consultingtypeservice.filter.HttpTenantFilter;
import de.caritas.cob.consultingtypeservice.filter.StatelessCsrfFilter;
import jakarta.annotation.Nullable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.csrf.CsrfFilter;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.security.web.util.matcher.NegatedRequestMatcher;

/** Provides the Security configuration. */
@Configuration
@EnableMethodSecurity
@EnableWebSecurity
public class SecurityConfig {

  @Value("${csrf.cookie.property}")
  private String csrfCookieProperty;

  @Value("${csrf.header.property}")
  private String csrfHeaderProperty;

  @Value("${multitenancy.enabled}")
  private boolean multitenancy;

  @Autowired(required = false)
  private @Nullable HttpTenantFilter tenantFilter;

  @Autowired AuthorisationService authorisationService;
  @Autowired JwtAuthConverterProperties jwtAuthConverterProperties;

  public static final String[] WHITE_LIST =
      new String[] {
        "/consultingtypes/docs",
        "/consultingtypes/docs/**",
        "/v2/api-docs",
        "/configuration/ui",
        "/swagger-resources/**",
        "/configuration/security",
        "/swagger-ui.html",
        "/webjars/**",
        "/actuator/health",
        "/actuator/health/**"
      };

  /** Configure spring security filter chain */
  @Bean
  SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
    var httpSecurity =
        http.csrf(csrf -> csrf.disable())
            .addFilterBefore(
                new StatelessCsrfFilter(csrfCookieProperty, csrfHeaderProperty), CsrfFilter.class);

    httpSecurity = enableTenantFilterIfMultitenancyEnabled(httpSecurity);
    httpSecurity
        .sessionManagement(
            management -> management.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
        .authorizeHttpRequests(
            requests ->
                requests
                    .requestMatchers(new AntPathRequestMatcher("/settings"))
                    .permitAll()
                    .requestMatchers(new AntPathRequestMatcher("/settings/*"))
                    .permitAll()
                    .requestMatchers(new AntPathRequestMatcher("/topic/public"))
                    .permitAll()
                    .requestMatchers(new AntPathRequestMatcher("/topic/public/*"))
                    .permitAll()
                    .requestMatchers(new AntPathRequestMatcher("/topic"))
                    .authenticated()
                    .requestMatchers(new AntPathRequestMatcher("/topic/*"))
                    .authenticated()
                    .requestMatchers(new AntPathRequestMatcher("/topicadmin"))
                    .authenticated()
                    .requestMatchers(new AntPathRequestMatcher("/topicadmin/*"))
                    .authenticated()
                    .requestMatchers(new AntPathRequestMatcher("/settingsadmin"))
                    .authenticated()
                    .requestMatchers(new AntPathRequestMatcher("/settingsadmin/*"))
                    .authenticated()
                    .requestMatchers(new NegatedRequestMatcher(new AntPathRequestMatcher("/topic")))
                    .permitAll()
                    .requestMatchers(
                        new NegatedRequestMatcher(new AntPathRequestMatcher("/topic/*")))
                    .permitAll()
                    .requestMatchers(
                        new NegatedRequestMatcher(new AntPathRequestMatcher("/topicadmin")))
                    .permitAll()
                    .requestMatchers(
                        new NegatedRequestMatcher(new AntPathRequestMatcher("/topicadmin/*")))
                    .permitAll()
                    .anyRequest()
                    .permitAll())
        .headers(
            headers ->
                headers
                    .xssProtection(withDefaults())
                    .contentSecurityPolicy(policy -> policy.policyDirectives("script-src 'self'")));

    httpSecurity.oauth2ResourceServer(
        server -> server.jwt(jwt -> jwt.jwtAuthenticationConverter(jwtAuthConverter())));
    return http.build();
  }

  /**
   * Adds additional filter for tenant feature if enabled that sets tenant_id into current thread.
   *
   * @param httpSecurity
   * @return
   */
  private HttpSecurity enableTenantFilterIfMultitenancyEnabled(HttpSecurity httpSecurity) {
    if (multitenancy) {
      httpSecurity = httpSecurity.addFilterAfter(this.tenantFilter, StatelessCsrfFilter.class);
    }
    return httpSecurity;
  }

  @Bean
  JwtAuthConverter jwtAuthConverter() {
    return new JwtAuthConverter(jwtAuthConverterProperties, authorisationService);
  }
}
