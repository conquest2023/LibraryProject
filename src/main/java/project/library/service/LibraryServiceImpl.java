package project.library.service;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.geo.Circle;
import org.springframework.data.geo.Distance;
import org.springframework.data.geo.GeoResults;
import org.springframework.data.geo.Point;
import org.springframework.data.redis.connection.RedisGeoCommands;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.domain.geo.GeoLocation;
import org.springframework.http.*;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;
import project.library.controller.dto.LocationDto;
import project.library.controller.dto.book.NearestLibrary;
import project.library.controller.dto.book.check.BookCheck;
import project.library.controller.dto.book.search.BookSearchReseponseDto;
import project.library.repository.LibraryRepository;
import project.library.repository.collection.Library;
import java.net.URI;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class LibraryServiceImpl implements  LibraryService{

    @Qualifier("externalApiTaskExecutor")
    private final Executor externalApiTaskExecutor;


    private final RedisTemplate redisTemplate;

    private final RestTemplate restTemplate;

    private final LibraryRepository repository;


    private  static final  String  REDIS_GEOKEY="libraries:locations";

    private static final String API_BASE_URL = "http://data4library.kr/api";

    private static final String AUTH_KEY = "1df2f040d9555558e014f541e2908356008ca9e3aa7a1d9c43ec2c15e54f5f4b";


    public List<Library> calculateDistance(LocationDto locationDto) {

        Point userLocation = new Point(locationDto.getLongitude(), locationDto.getLatitude());
        Distance radius = new Distance(5, RedisGeoCommands.DistanceUnit.KILOMETERS);
        Circle area = new Circle(userLocation, radius);
        GeoResults<RedisGeoCommands.GeoLocation<String>> results =
                redisTemplate.opsForGeo().radius(
                        "libraries:locations",
                        area,
                        RedisGeoCommands.GeoRadiusCommandArgs.newGeoRadiusArgs().includeCoordinates()
                );



        Map<String, Point> coordByLib = results.getContent().stream()
                .filter(r -> r.getContent().getPoint() != null)   // 좌표 없는 건 제외
                .collect(Collectors.toMap(
                        r -> r.getContent().getName(),
                        r -> r.getContent().getPoint()
                ));

        List<String> libraryCodes = new ArrayList<>(coordByLib.keySet());
        log.info("반경 내 도서관 {}개 발견.", libraryCodes.size());


        // 2) 외부 API 병렬 호출 결과 수집
        List<AbstractMap.SimpleEntry<String, BookSearchReseponseDto>> apiResults =
                getCompletableFutureList(locationDto, libraryCodes).join();

        // 3) Y/Y 필터 + 거리 계산
        double uLat = locationDto.getLatitude();
        double uLon = locationDto.getLongitude();

        List<NearestLibrary> computed = apiResults.stream()
                .filter(e -> e.getValue() != null && e.getValue().getResponse() != null)
                .filter(e -> {
                    var r = e.getValue().getResponse().getResult();
                    return "Y".equals(r.getHasBook()) && "Y".equals(r.getLoanAvailable());
                })
                .map(e -> {
                    String lib = e.getKey();
                    Point p = coordByLib.get(lib);       // 재조회 없음!
                    if (p == null) return null;
                    double km = haversine(uLat, uLon, p.getY(), p.getX());
                    return new NearestLibrary(lib, p.getY(), p.getX(), km);
                })
                .filter(Objects::nonNull)
                .sorted(Comparator.comparingDouble(NearestLibrary::getDistanceKm))
                .limit(3)
                .toList();

        log.info("TOP3: {}", computed);

        // 4) 엔티티로 매핑해서 반환
        return null;
    }



    @Async("externalApiTaskExecutor")
    public CompletableFuture<List<AbstractMap.SimpleEntry<String, BookSearchReseponseDto>>>
    getCompletableFutureList(LocationDto locationDto, List<String> libraryCodes) {

        List<CompletableFuture<AbstractMap.SimpleEntry<String, BookSearchReseponseDto>>> futures =
                libraryCodes.stream()
                        .map(libCode -> CompletableFuture
                                .supplyAsync(() -> callBookExist(libCode, locationDto.getIsbn()), externalApiTaskExecutor)
                                .completeOnTimeout(null, 3, java.util.concurrent.TimeUnit.SECONDS)
                                .exceptionally(ex -> {
                                    log.warn("API 실패: {}", libCode, ex);
                                    return null;
                                })
                        ).toList();
        return CompletableFuture
                .allOf(futures.toArray(new CompletableFuture[0]))
                .thenApply(v -> futures.stream()
                        .map(CompletableFuture::join)
                        .filter(Objects::nonNull)
                        .toList());
    }

    private AbstractMap.SimpleEntry<String, BookSearchReseponseDto> callBookExist(String libCode, String isbn) {
        URI uri = UriComponentsBuilder.fromUriString(API_BASE_URL)
                .pathSegment("bookExist")
                .queryParam("authKey", AUTH_KEY)
                .queryParam("libCode", libCode)
                .queryParam("isbn13", isbn)
                .queryParam("format", "json")
                .encode()
                .build()
                .toUri();

        HttpHeaders headers = new HttpHeaders();
        headers.setAccept(List.of(MediaType.ALL));
        HttpEntity<String> entity = new HttpEntity<>(headers);

        ResponseEntity<BookSearchReseponseDto> res =
                restTemplate.exchange(uri, HttpMethod.GET, entity, BookSearchReseponseDto.class);

        return new AbstractMap.SimpleEntry<>(libCode, res.getBody());
    }
    static double haversine(double lat1, double lon1, double lat2, double lon2) {
        double R = 6371.0; // km
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);
        double a = Math.sin(dLat/2) * Math.sin(dLat/2) +
                Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
                        Math.sin(dLon/2) * Math.sin(dLon/2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1-a));
        return R * c;
    }
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

        for (Library library : all) {
            // 각 도서관의 libCode를 Hash의 키로 사용
            String hashKey = "library:" + library.getLibCode();

            // 맵(Map)을 사용하여 모든 필드를 한 번에 저장
            Map<String, String> libraryData = new HashMap<>();
            libraryData.put("libName", library.getLibName());
            libraryData.put("address", library.getAddress());
            libraryData.put("tel", library.getTel());
            libraryData.put("homepage", library.getHomepage());
            libraryData.put("closed",library.getClosed());
            libraryData.put("libCode",library.getLibCode());
            // 필요에 따라 다른 필드들도 추가

            redisTemplate.opsForHash().putAll(hashKey, libraryData);
        }

    }
}
