package com.nyam.everyday.config;

import jakarta.servlet.http.HttpServletResponse;
import java.util.List;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.header.writers.ReferrerPolicyHeaderWriter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

@Slf4j
@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

  private static final String[] STATIC_RESOURCES = {
      "/", "/index.html", "/*.html", "/favicon.ico",
      "/css/**", "/fetchWithAuth.js", "/js/**", "/images/**", "/.well-known/**", "/error"
  };

  private static final String[] SWAGGER_RESOURCES = {
      "/v3/api-docs/**", "/swagger-ui/**", "/swagger-ui.html",
      "/swagger-resources/**", "/webjars/**", "/configuration/**"
  };

  private static final String[] PUBLIC_API_ROUTES = {
      "/api/auth/sign-up", "/api/auth/login", "/api/auth/logout",
      "/oauth2/**", "/login/**", "/actuator/prometheus", "/exception",
      "/api/member/**"
  };

  @Bean
  public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
    return http
        .cors(Customizer.withDefaults())
        .csrf(AbstractHttpConfigurer::disable)
        .headers(headers ->
            headers.referrerPolicy(policy ->
                policy.policy(ReferrerPolicyHeaderWriter.ReferrerPolicy.NO_REFERRER_WHEN_DOWNGRADE)
            )
        )
        .authorizeHttpRequests(auth -> auth
            .requestMatchers(STATIC_RESOURCES).permitAll()
            .requestMatchers(SWAGGER_RESOURCES).permitAll()
            .requestMatchers(PUBLIC_API_ROUTES).permitAll()
            .requestMatchers("/api/**").authenticated()
            .anyRequest().permitAll()
        )
        .exceptionHandling(e -> e
            .authenticationEntryPoint((request, response, authException) -> {
              String url = request.getRequestURI();
              log.warn("[SecurityConfig] Unauthorized => {}", url);
              response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Unauthorized");
            })
            .accessDeniedHandler((request, response, accessDeniedException) -> {
              String url = request.getRequestURI();
              log.warn("[SecurityConfig] Forbidden => {}", url);
              response.sendError(HttpServletResponse.SC_FORBIDDEN, "Forbidden");
            })
        )
        .build();
  }

  @Bean
  public CorsConfigurationSource corsConfigurationSource() {
    CorsConfiguration config = new CorsConfiguration();
    config.setAllowCredentials(true);
    config.setAllowedOrigins(List.of("http://localhost:3000"));
    config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
    config.setAllowedHeaders(List.of("*"));
    config.setExposedHeaders(List.of("Authorization"));

    UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
    source.registerCorsConfiguration("/**", config);
    return source;
  }
}