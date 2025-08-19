package com.nyam.everyday.redis.config;


import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import org.springframework.security.jackson2.SecurityJackson2Modules;
import org.springframework.security.oauth2.client.jackson2.OAuth2ClientJackson2Module;

@Configuration
@RequiredArgsConstructor
public class RedisConfig {

    @Value("${spring.data.redis.host}")
    private String host;

    @Value("${spring.data.redis.port}")
    private int port;

    @Bean
    public RedisConnectionFactory redisConnectionFactory() {
        RedisStandaloneConfiguration configuration = new RedisStandaloneConfiguration();
        configuration.setHostName(host);
        configuration.setPort(port);

        return new LettuceConnectionFactory(configuration);
    }

    /** Security 모듈이 등록된 ObjectMapper (OAuth2 타입 역직렬화 지원) */
    @Bean
    public ObjectMapper redisSecurityObjectMapper() {
        ObjectMapper om = new ObjectMapper();
        // Spring Security 관련 타입들(AuthorizationGrantType, ClientAuthenticationMethod 등)
        om.registerModules(SecurityJackson2Modules.getModules(getClass().getClassLoader()));
        // OAuth2AuthorizationRequest / OAuth2AuthorizationResponseType 지원
        om.registerModule(new OAuth2ClientJackson2Module());
        return om;
    }

    @Bean
    public RedisTemplate<String, Object> redisTemplate(
            RedisConnectionFactory connectionFactory,
            ObjectMapper redisSecurityObjectMapper
    ) {
        GenericJackson2JsonRedisSerializer json = new GenericJackson2JsonRedisSerializer(redisSecurityObjectMapper);

        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);

        // Key/HashKey는 문자열
        template.setKeySerializer(new StringRedisSerializer());
        template.setHashKeySerializer(new StringRedisSerializer());

        // Value/HashValue는 Security 모듈이 포함된 Jackson 직렬화기
        template.setValueSerializer(json);
        template.setHashValueSerializer(json);

        template.afterPropertiesSet();
        return template;
    }

}
