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
import org.springframework.http.*;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;
import project.library.controller.dto.LocationDto;
import project.library.controller.dto.book.NearestLibrary;
import project.library.controller.dto.book.search.BookSearchReseponseDto;
import project.library.repository.LibraryRepository;
import project.library.repository.collection.Library;

import java.net.URI;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class CalculateServiceImpl implements CalculateService{

    @Qualifier("externalApiTaskExecutor")
    private final Executor externalApiTaskExecutor;


    private final RedisTemplate redisTemplate;

    private final LibraryRepository repository;

    private final RestTemplate restTemplate;


    private static final String API_BASE_URL = "http://data4library.kr/api";

    private static final String AUTH_KEY = "1df2f040d9555558e014f541e2908356008ca9e3aa7a1d9c43ec2c15e54f5f4b";
    @Override
    public List<NearestLibraryDetail> calculateDistance(LocationDto locationDto) {


            Point userLocation = new Point(locationDto.getLongitude(), locationDto.getLatitude());
            Distance radius = new Distance(3, RedisGeoCommands.DistanceUnit.KILOMETERS);
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
                            r -> String.valueOf(r.getContent().getName()),
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
                        return "Y".equals(r.getHasBook());
//                     && "Y".equals(r.getLoanAvailable()
                    })
                    .map(e -> {
                        String lib = e.getKey();
                        String isLoan = e.getValue().getResponse().getResult().getLoanAvailable();
                        Point p = coordByLib.get(lib);
                        if (p == null)
                            return null;
                        double km = haversine(uLat, uLon, p.getY(), p.getX());
                        return new NearestLibrary(lib, isLoan, p.getY(), p.getX(), km);
                    })
                    .filter(Objects::nonNull)
                    .sorted(Comparator.comparingDouble(NearestLibrary::getDistanceKm))
                    .limit(5)
                    .toList();

            List<Integer> libCodes = computed.stream()
                    .map(lib -> Integer.parseInt(lib.getLibCode())) // String → int 파싱
                    .toList();

            log.info("TOP5: {}", computed);
            List<Library> libs = repository.findByLibCodeIn(libCodes);
            Map<String, Library> libMap = libs.stream()
                    .collect(Collectors.toMap(l ->
                                    String.valueOf(
                                            l.getLibCode()),
                            Function.identity(), (a, b) -> a));

            List<NearestLibraryDetail> result = new ArrayList<>();
            for (NearestLibrary n : computed) {
                Library lib = libMap.get(n.getLibCode());
                if (lib != null) {
                    result.add(new NearestLibraryDetail(
                            n.getLibCode(),
                            lib.getLibName(),
                            n.getIsLoan(),
                            lib.getAddress(),
                            lib.getTel(),
                            lib.getLatitude(),
                            lib.getLongitude(),
                            n.getDistanceKm()
                    ));
                }
            }
            return result;

    }

        @Async("externalApiTaskExecutor")
        public CompletableFuture<List<AbstractMap.SimpleEntry<String, BookSearchReseponseDto>>>
        getCompletableFutureList(LocationDto locationDto, List < String > libraryCodes) {

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

        private AbstractMap.SimpleEntry<String, BookSearchReseponseDto> callBookExist (String libCode, String isbn){
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

        static double haversine ( double lat1, double lon1, double lat2, double lon2){
            double R = 6371.0;
            double dLat = Math.toRadians(lat2 - lat1);
            double dLon = Math.toRadians(lon2 - lon1);
            double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                    Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
                            Math.sin(dLon / 2) * Math.sin(dLon / 2);
            double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
            return R * c;
        }
}
