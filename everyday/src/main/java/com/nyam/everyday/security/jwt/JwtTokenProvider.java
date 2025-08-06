package com.nyam.everyday.security.jwt;

import com.nyam.everyday.security.core.CustomUserDetails;
import io.jsonwebtoken.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtTokenProvider {

  private final SecretKey secretKey;

  /**
   * JWT 토큰 생성
   *
   * @param authentication 인증 정보
   * @param expirationMillis 토큰 만료 시간 (ms)
   * @return JWT 토큰 문자열
   */
  public String generateToken(Authentication authentication, Long expirationMillis) {
    CustomUserDetails customUserDetails = (CustomUserDetails) authentication.getPrincipal();

    Date now = new Date();
    Date expiryDate = new Date(now.getTime() + expirationMillis);

    Claims claims = Jwts.claims();
    claims.put("member-id", customUserDetails.getId());
    claims.put("providerid", customUserDetails.getUsername());

    return Jwts.builder()
        .setSubject(customUserDetails.getUsername())
        .setClaims(claims)
        .setIssuedAt(now)
        .setExpiration(expiryDate)
        .signWith(secretKey, SignatureAlgorithm.HS512)
        .compact();
  }

  /**
   * JWT 토큰에서 사용자 ID 추출
   *
   * @param token JWT 토큰
   * @return 사용자 ID
   */
  public Long getUserIdFromToken(String token) {
    return Jwts.parserBuilder()
        .setSigningKey(secretKey)
        .build()
        .parseClaimsJws(token)
        .getBody()
        .get("member-id", Long.class);
  }

  /**
   * 토큰 유효성 검사
   *
   * @param token JWT 토큰
   * @return 유효하면 true
   */
  public boolean validateToken(String token) {
    try {
      Jwts.parserBuilder()
          .setSigningKey(secretKey)
          .build()
          .parseClaimsJws(token);
      return true;
    } catch (MalformedJwtException e) {
      log.error("Invalid JWT token format: {}", e.getMessage());
    } catch (ExpiredJwtException e) {
      log.error("JWT token has expired: {}", e.getMessage());
    } catch (UnsupportedJwtException e) {
      log.error("JWT token is unsupported: {}", e.getMessage());
    } catch (IllegalArgumentException e) {
      log.error("JWT claims string is empty or illegal: {}", e.getMessage());
    } catch (JwtException e) {
      log.error("JWT validation error: {}", e.getMessage());
    }
    return false;
  }
}