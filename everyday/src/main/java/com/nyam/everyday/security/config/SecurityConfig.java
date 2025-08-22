package com.nyam.everyday.security.config;

import com.nyam.everyday.oauth2.OAuth2LoginSuccessHandler;
import com.nyam.everyday.oauth2.OAuth2UserService;
import com.nyam.everyday.oauth2.RedisOAuth2AuthorizationRequestRepository;
import com.nyam.everyday.security.jwt.JwtTokenFilter;
import jakarta.servlet.http.HttpServletResponse;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.client.web.AuthorizationRequestRepository;
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationRequest;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.header.writers.ReferrerPolicyHeaderWriter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

@Slf4j
@Configuration
@EnableWebSecurity
public class SecurityConfig {

  private final JwtTokenFilter jwtTokenFilter;
  private final OAuth2LoginSuccessHandler oAuth2LoginSuccessHandler;
  private final OAuth2UserService oAuth2UserService;
  private final RedisTemplate<String, Object> redisTemplate;

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
      "/api/boards/**","/api/board-comments/**"
  };

  public SecurityConfig(
      JwtTokenFilter jwtTokenFilter,
      OAuth2LoginSuccessHandler oAuth2LoginSuccessHandler,
      OAuth2UserService oAuth2UserService,
      @Qualifier("redisLoginTemplate") RedisTemplate<String, Object> redisTemplate
  ) {
    this.jwtTokenFilter = jwtTokenFilter;
    this.oAuth2LoginSuccessHandler = oAuth2LoginSuccessHandler;
    this.oAuth2UserService = oAuth2UserService;
    this.redisTemplate = redisTemplate;
  }

  @Bean
  public AuthorizationRequestRepository<OAuth2AuthorizationRequest> authorizationRequestRepository() {
    return new RedisOAuth2AuthorizationRequestRepository(redisTemplate);
  }

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
            .requestMatchers("/ws/**").permitAll()
            .requestMatchers("/ws").permitAll()
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
        .sessionManagement(session -> session
            .sessionCreationPolicy(SessionCreationPolicy.STATELESS))

        .addFilterBefore(jwtTokenFilter, UsernamePasswordAuthenticationFilter.class)

        .oauth2Login(oauth2 -> oauth2
            .loginPage("/index.html")
            .userInfoEndpoint(userInfo -> userInfo.userService(oAuth2UserService))
            .successHandler(oAuth2LoginSuccessHandler)

            .authorizationEndpoint(authorization -> authorization
                .authorizationRequestRepository(authorizationRequestRepository()))
        )
//          .logout(logout -> logout
//            .logoutUrl("/api/auth/logout")
//            .addLogoutHandler(oAuth2LogoutHandler)
//            .logoutSuccessHandler(oAuth2LogoutSuccessHandler)
//            .permitAll()
//        )

        .build();
  }

  @Bean
  public CorsConfigurationSource corsConfigurationSource() {
    CorsConfiguration config = new CorsConfiguration();
    config.setAllowCredentials(true);
    config.setAllowedOrigins(List.of("http://localhost:3000", "http://localhost:5173","http://localhost:8081"));
    config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
    config.setAllowedHeaders(List.of("*"));
    config.setExposedHeaders(List.of("Authorization"));

    UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
    source.registerCorsConfiguration("/**", config);
    return source;
  }
}