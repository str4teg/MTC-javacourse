package com.mipt.andreysofronov.gateway.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mipt.andreysofronov.gateway.security.JwtAuthenticationFilter;
import com.mipt.andreysofronov.gateway.security.JwtService;
import com.mipt.andreysofronov.gateway.security.PepperBCryptPasswordEncoder;
import com.mipt.andreysofronov.gateway.security.RestAccessDeniedHandler;
import com.mipt.andreysofronov.gateway.security.RestAuthenticationEntryPoint;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * Stateless JWT security for the internal {@code /api/v1/**} API (lecture 9).
 *
 * <ul>
 *   <li>{@link SecurityFilterChain} bean — no {@code WebSecurityConfigurerAdapter}.
 *   <li>{@link SessionCreationPolicy#STATELESS} + CSRF disabled (safe because the API is stateless
 *       and token-based, with no cookies/sessions to forge).
 *   <li>A {@link JwtAuthenticationFilter} placed before the username/password filter.
 *   <li>Role / authority rules: {@code /api/v1/profile} → {@code ROLE_USER},
 *       {@code /api/v1/docs} → {@code READ_PRIVILEGE}.
 *   <li>JSON 401 / 403 via a custom entry point and access-denied handler.
 * </ul>
 *
 * <p>Endpoints outside {@code /api/v1/**} (legacy {@code /api/tasks}, the {@code /external/**}
 * emulator, swagger, actuator) are left open so the scope of this security config is exactly the
 * internal v1 API.
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

  @Bean
  public SecurityFilterChain securityFilterChain(
      HttpSecurity http, JwtService jwtService, ObjectMapper objectMapper) throws Exception {

    JwtAuthenticationFilter jwtFilter = new JwtAuthenticationFilter(jwtService);

    http.csrf(csrf -> csrf.disable())
        .cors(cors -> {})
        .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
        .authorizeHttpRequests(
            auth ->
                auth.requestMatchers("/api/v1/auth/**")
                    .permitAll()
                    .requestMatchers("/api/v1/profile")
                    .hasRole("USER")
                    .requestMatchers("/api/v1/docs")
                    .hasAuthority("READ_PRIVILEGE")
                    .requestMatchers("/api/v1/**")
                    .authenticated()
                    // Debugging emulator, actuator and API docs stay open.
                    .requestMatchers("/external/**", "/actuator/**")
                    .permitAll()
                    .requestMatchers(
                        "/swagger-ui/**", "/swagger-ui.html", "/v3/api-docs/**", "/v3/api-docs.yaml")
                    .permitAll()
                    // Legacy (non-v1) endpoints of this app remain accessible.
                    .anyRequest()
                    .permitAll())
        .exceptionHandling(
            ex ->
                ex.authenticationEntryPoint(new RestAuthenticationEntryPoint(objectMapper))
                    .accessDeniedHandler(new RestAccessDeniedHandler(objectMapper)))
        .httpBasic(basic -> basic.disable())
        .formLogin(form -> form.disable())
        .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);

    return http.build();
  }

  @Bean
  public PasswordEncoder passwordEncoder(@Value("${security.password.pepper}") String pepper) {
    return new PepperBCryptPasswordEncoder(pepper);
  }

  /**
   * Two in-memory users. Passwords are stored only as BCrypt(pepper + raw) digests — never in clear
   * text. {@code reader} additionally carries the {@code READ_PRIVILEGE} authority.
   */
  @Bean
  public UserDetailsService userDetailsService(PasswordEncoder passwordEncoder) {
    UserDetails user =
        User.withUsername("user")
            .password(passwordEncoder.encode("password"))
            .authorities("ROLE_USER")
            .build();

    UserDetails reader =
        User.withUsername("reader")
            .password(passwordEncoder.encode("password"))
            .authorities("ROLE_USER", "READ_PRIVILEGE")
            .build();

    return new InMemoryUserDetailsManager(user, reader);
  }
}
