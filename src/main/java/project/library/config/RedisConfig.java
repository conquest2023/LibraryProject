package project.library.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

@Configuration
@Slf4j
public class RedisConfig {

    @Value("${spring.data.redis.host}")
    private String redisHost;

    @Value("${spring.data.redis.port}")
    private int redisPort;

    @Bean
    public LettuceConnectionFactory redisConnectionFactory() {
        return new LettuceConnectionFactory(new RedisStandaloneConfiguration(redisHost, redisPort));
    }

    @Bean
    public RedisTemplate<String, Object> redisTemplate() {
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(redisConnectionFactory());
        template.setKeySerializer(new StringRedisSerializer());
        StringRedisSerializer stringSer = new StringRedisSerializer();
        template.setKeySerializer(stringSer);
        template.setHashKeySerializer(stringSer);

        // ── value 쪽 직렬화기 : Jackson ➜ LZ4 래핑 ──────
        RedisSerializer<Object> jsonSer = new GenericJackson2JsonRedisSerializer();
//        RedisSerializer<Object> lz4Ser  = new Lz4RedisSerializer<>(jsonSer);

        // ★ 한 번만 지정!  (List·Value 계열)
//        template.setValueSerializer(lz4Ser);

        // ★ Hash 값도 동일 직렬화기
//        template.setHashValueSerializer(lz4Ser);

        // Spring Data Redis 3.2+ (Boot 3.3+) 이면 List 전용 직렬화기도 명시 가능
        // template.setListValueSerializer(lz4Ser);

        template.afterPropertiesSet();
        template.setValueSerializer(new GenericJackson2JsonRedisSerializer());
        return template;
    }
}
