package project.library.service.domain;

import org.springframework.data.geo.Circle;
import org.springframework.data.geo.GeoResults;
import org.springframework.data.geo.Point;
import org.springframework.data.redis.connection.RedisGeoCommands;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import project.library.controller.dto.UserLocation;
import project.library.repository.collection.Library;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class LibraryFinder {

    private final RedisTemplate<String, String> redisTemplate;

    public LibraryFinder(RedisTemplate<String, String> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    public Map<String ,Point> findNearby(UserLocation location, double radiusKm) {
        Circle area =location.toCircle(radiusKm);
        GeoResults<RedisGeoCommands.GeoLocation<String>> results =
                redisTemplate.opsForGeo().radius(
                        "libraries:locations",
                        area,
                        RedisGeoCommands.GeoRadiusCommandArgs.newGeoRadiusArgs().includeCoordinates()
                );

         return results.getContent().stream()
//                    .filter(r -> r.getContent().getPoint() != null)   // 좌표 없는 건 제외
                .collect(Collectors.toMap(
                        r -> String.valueOf(r.getContent().getName()),
                        r -> r.getContent().getPoint()
                ));

    }

}
