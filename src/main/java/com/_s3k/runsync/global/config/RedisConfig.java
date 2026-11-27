package com._s3k.runsync.global.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceClientConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;

@Configuration
@Profile("local")
public class RedisConfig {

    @Value("${spring.data.redis.host}")
    private String host;

    @Value("${spring.data.redis.port}")
    private int port;

    // TLS 를 지원하지 않는 Redis(로컬 설치본 등)에 붙을 때는 false 로 둔다.
    // TLS 로 접속하면 서버가 응답하지 않아 Redis 호출이 제한 시간까지 멈춘다.
    @Value("${spring.data.redis.ssl.enabled:true}")
    private boolean sslEnabled;

    @Bean
    public RedisConnectionFactory redisConnectionFactory() {
        RedisStandaloneConfiguration serverConfig = new RedisStandaloneConfiguration(host, port);

        LettuceClientConfiguration.LettuceClientConfigurationBuilder builder = LettuceClientConfiguration.builder();
        if (sslEnabled) {
            builder.useSsl().disablePeerVerification();
        }
        LettuceClientConfiguration clientConfig = builder.build();

        return new LettuceConnectionFactory(serverConfig, clientConfig);
    }
}
