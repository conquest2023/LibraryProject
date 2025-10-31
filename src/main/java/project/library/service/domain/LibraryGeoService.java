package project.library.service.domain;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.geo.Point;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import project.library.repository.LibraryJpaRepository;
import project.library.repository.collection.Library;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class LibraryGeoService {

    private final LibraryJpaRepository repository;

    private  final RedisTemplate redisTemplate;

    public static final Cache<Integer, Library> cache = Caffeine.newBuilder()
            .maximumSize(2000)
            .build();
//    private final Caff
    public void putGeotRedisLibrary(){
        List<Library> all = repository.findAll();
        for (Library library : all) {
            redisTemplate.opsForGeo().add(
                    "libraries:locations", // Redis Key
                    new Point(library.getLongitude(), library.getLatitude()), // GeoLocation 객체
                    library.getLibCode() // Member
            );
        }
    }

    public void putRedisLibrary(){
        List<Library> all = repository.findAll();
        Map<String, String> libraryData = new HashMap<>();
        for (Library library : all) {
            // 각 도서관의 libCode를 Hash의 키로 사용
            String hashKey = "library:" + library.getLibCode();
            libraryData.put("libName", library.getLibName());
            libraryData.put("address", library.getAddress());
            libraryData.put("tel", library.getTel());
            libraryData.put("homepage", library.getHomepage());
            libraryData.put("closed",library.getClosed());
//            libraryData.put("libCode",String.valueOf(library.getLibCode()));
            redisTemplate.opsForHash().putAll(hashKey, libraryData);
        }
    }


    public void putLocalLibrary(){
        List<Library> all = repository.findAll();
        for (Library library : all) {

            cache.put(library.getLibCode(),library);
        }
        log.info("캐시 warm 성공");
    }

    @PostConstruct
    public void initCache() {
        putLocalLibrary();
    }

}
