package com.fairticket.global.config;

import java.time.Duration;

import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

@Configuration
@EnableCaching
public class RedisConfig {
	
	@Bean
	public RedisCacheManager cacheManager(RedisConnectionFactory connectionFactory) {
	    // ObjectMapper 생성 (JavaTimeModule 포함)
	    ObjectMapper objectMapper = new ObjectMapper();
	    objectMapper.registerModule(new JavaTimeModule());
	    objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
	    
	    GenericJackson2JsonRedisSerializer serializer = 
	            new GenericJackson2JsonRedisSerializer(objectMapper);
	    
	    RedisCacheConfiguration config = RedisCacheConfiguration.defaultCacheConfig()
	            .entryTtl(Duration.ofMinutes(10))
	            .serializeKeysWith(RedisSerializationContext.SerializationPair
	                    .fromSerializer(new StringRedisSerializer()))
	            .serializeValuesWith(RedisSerializationContext.SerializationPair
	                    .fromSerializer(serializer));  // ← 커스텀 serializer 사용!
	    
	    return RedisCacheManager.builder(connectionFactory)
	            .cacheDefaults(config)
	            .build();
	}
    
    //캐시 직렬화 오류 해결 
    @Bean
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory connectionFactory) {
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);
        
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        
        GenericJackson2JsonRedisSerializer serializer = 
                new GenericJackson2JsonRedisSerializer(objectMapper);
        
        template.setKeySerializer(new StringRedisSerializer());
        template.setValueSerializer(serializer);
        template.setHashKeySerializer(new StringRedisSerializer());
        template.setHashValueSerializer(serializer);
        
        return template;
    }
}