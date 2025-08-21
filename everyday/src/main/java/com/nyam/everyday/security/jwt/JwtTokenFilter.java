package com.nyam.everyday.security.jwt;

import com.nyam.everyday.module.member.entity.Member;
import com.nyam.everyday.module.member.entity.Status;
import com.nyam.everyday.module.member.repository.MemberRepository;
import com.nyam.everyday.security.core.CustomUserDetailsService;
import com.nyam.everyday.security.threadlocal.TraceIdHolder;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtTokenFilter extends OncePerRequestFilter {

  private final JwtTokenProvider jwtTokenProvider;
  private final CustomUserDetailsService customUserDetailsService;
  private final MemberRepository memberRepository;

  @Override
  protected boolean shouldNotFilter(HttpServletRequest request) {
    String path = request.getRequestURI();
    return path.startsWith("/css/") ||
        path.startsWith("/js/") ||
        path.startsWith("/images/") ||
        path.equals("/") ||
        path.equals("/index.html") ||
        path.endsWith(".html") ||
        path.startsWith("/favicon.ico") ||
        path.startsWith("/api/auth/") ||
        path.startsWith("/error") ||
        path.startsWith("/fetchWithAuth.js") ||
        path.startsWith("/swagger-ui") ||
        path.startsWith("/v3/api-docs") ||
        path.startsWith("/actuator/prometheus");
  }

  @Override
  protected void doFilterInternal(HttpServletRequest request,
      HttpServletResponse response,
      FilterChain filterChain
  ) throws ServletException, IOException {

    String traceId = UUID.randomUUID().toString().substring(0, 8);
    TraceIdHolder.set(traceId);

    log.info("[{}] JwtTokenFilter 진입: 요청 URI = {}", traceId, request.getRequestURI());

    try {
      String accessToken = getTokenFromRequest(request);

      if (accessToken != null && jwtTokenProvider.validateToken(accessToken)) {
        log.info("[{}] Access Token 유효성 검증 성공.", traceId);

        Long memberId = jwtTokenProvider.getUserIdFromToken(accessToken);
        Member member = memberRepository.findById(memberId).orElse(null);

        if (member == null) {
          SecurityContextHolder.clearContext();
          writeUnauthorized(response,"MEMBER_NOT_FOUND");
          return;
        }
        if (member.getMemberStatus() == Status.DEACTIVATED){
          SecurityContextHolder.clearContext();
          writeForbidden(response,"MEMBER_DEACTIVATED");
          return;
        }

        UsernamePasswordAuthenticationToken authenticationToken = getAuthentication(accessToken);
        authenticationToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
        SecurityContextHolder.getContext().setAuthentication(authenticationToken);

        log.info("[{}] SecurityContextHolder에 Authentication 설정 완료. 유저: {}", traceId, authenticationToken.getName());
      } else {
        SecurityContextHolder.clearContext();
      }

      filterChain.doFilter(request, response);

    } catch (Exception e) {
      log.error("[{}] JwtTokenFilter 처리 중 예외 발생: {}", traceId, e.getMessage(), e);
      SecurityContextHolder.clearContext();
      filterChain.doFilter(request, response);
    } finally {
      TraceIdHolder.clear();
    }
  }

  private String getTokenFromRequest(HttpServletRequest request) {
    String bearerToken = request.getHeader("Authorization");
    if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
      return bearerToken.substring(7);
    }

    if (request.getCookies() != null) {
      for (Cookie cookie : request.getCookies()) {
        if ("accessToken".equals(cookie.getName())) {
          return cookie.getValue();
        }
      }
    }
    return null;
  }

  private UsernamePasswordAuthenticationToken getAuthentication(String token) {
    Long memberId = jwtTokenProvider.getUserIdFromToken(token);
    log.info("[getAuthentication] 토큰에서 추출된 memberId: {}", memberId);
    UserDetails userDetails = customUserDetailsService.loadUserByMemberId(memberId);
    return new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
  }

  private void writeForbidden(HttpServletResponse res, String code) throws IOException {
    res.setStatus(HttpServletResponse.SC_FORBIDDEN);               // 403
    res.setContentType("application/json;charset=UTF-8");
    res.getWriter().write("{\"error\":\"" + code + "\"}");
  }

  private void writeUnauthorized(HttpServletResponse res, String code) throws IOException {
    res.setStatus(HttpServletResponse.SC_UNAUTHORIZED);            // 401
    res.setContentType("application/json;charset=UTF-8");
    res.getWriter().write("{\"error\":\"" + code + "\"}");
  }
}