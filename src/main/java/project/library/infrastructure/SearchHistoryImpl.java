package project.library.infrastructure;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;


@Component
@RequiredArgsConstructor
public class SearchHistoryImpl implements SearchHistoryPort {


    private static final String KEY_PREFIX = "history:";
    private static final ZoneId ZONE = ZoneId.of("Asia/Seoul");
    private static final Duration TTL = Duration.ofDays(7);

    private final StringRedisTemplate redisTemplate;
    private static final long MAX_ITEMS = 100;

    @Override
    public void addHistory(String sessionId, String book) {
        String key = KEY_PREFIX + sessionId;
        double score = LocalDate.now(ZONE).toEpochDay();
        ZSetOperations<String, String> zset = redisTemplate.opsForZSet();
        zset.add(key, book, score);
        redisTemplate.expire(sessionId, TTL);
    }

    @Override
    public List<String> getHistory(String sessionId) {
        String key = KEY_PREFIX + sessionId;
        Set<String> set = redisTemplate.opsForZSet().reverseRange(key, 0, -1);
        if (set == null || set.isEmpty()) return List.of();
        return new ArrayList<>(set);
    }
}
