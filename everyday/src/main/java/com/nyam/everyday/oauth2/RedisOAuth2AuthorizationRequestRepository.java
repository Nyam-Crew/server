package com.nyam.everyday.oauth2;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.oauth2.client.web.AuthorizationRequestRepository;
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationRequest;

import java.time.Duration;


@RequiredArgsConstructor
public class RedisOAuth2AuthorizationRequestRepository implements AuthorizationRequestRepository<OAuth2AuthorizationRequest> {

  private static final String PREFIX = "oauth2_auth_request:";

  private final RedisTemplate<String, Object> redisTemplate;

  @Override
  public OAuth2AuthorizationRequest loadAuthorizationRequest(HttpServletRequest request) {
    String state = request.getParameter("state");
    if (state == null) return null;

    return (OAuth2AuthorizationRequest) redisTemplate.opsForValue().get(PREFIX + state);
  }


  @Override
  public void saveAuthorizationRequest(
      OAuth2AuthorizationRequest authorizationRequest,
      HttpServletRequest request,
      HttpServletResponse response) {

    if (authorizationRequest == null) return;
    String state = authorizationRequest.getState();
    // Redis에 10분동안 저장(10분 뒤 삭제됨)
    redisTemplate.opsForValue().set(
        PREFIX +
            state,
        authorizationRequest,
        Duration.ofMinutes(10));

  }

  /**
   * [3] 인가 요청 삭제하기
   * 인증 과정이 끝나면 Redis에서 해당 요청 정보를 제거함
   */
  @Override
  public OAuth2AuthorizationRequest removeAuthorizationRequest(
      HttpServletRequest request,
      HttpServletResponse response) {
    String state = request.getParameter("state");
    if (state == null) return null;
    String key = PREFIX + state;

    OAuth2AuthorizationRequest authRequest = (OAuth2AuthorizationRequest) redisTemplate.opsForValue().get(key);

    redisTemplate.delete(key);
    return authRequest;
  }
}