package project.library.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.geo.Point;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import project.library.repository.LibraryRepository;
import project.library.repository.collection.Library;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class LibraryGeoService {

    private final LibraryRepository repository;

    private final RedisTemplate redisTemplate;
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
}
