package project.library.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.geo.Point;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;
import project.library.controller.dto.UserLocation;
import project.library.controller.dto.book.NearestLibrary;
import project.library.controller.dto.book.search.BookSearchReseponseDto;
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


//    private final RedisTemplate redisTemplate;

//    private final LibraryJpaRepository repository;

    private final RestTemplate restTemplate;

    private final LibraryFinder finder;

    private static final String API_BASE_URL = "http://data4library.kr/api";

    private static final String AUTH_KEY = "1df2f040d9555558e014f541e2908356008ca9e3aa7a1d9c43ec2c15e54f5f4b";
    @Override
    public List<NearestLibraryDetail> findNearbyLibrary(UserLocation userLocation) {


            Map<String, Point> nearby = finder.findNearby(userLocation, 3);

            List<String> libraryCodes = new ArrayList<>(nearby.keySet());

            log.info("반경 내 도서관 {}개 발견.", libraryCodes.size());


            // 2) 외부 API 병렬 호출 결과 수집
            List<AbstractMap.SimpleEntry<String, BookSearchReseponseDto>> apiResults =
                    getCompletableFutureList(userLocation, libraryCodes).join();

            double uLat = userLocation.getLatitude();
            double uLon = userLocation.getLongitude();

            List<NearestLibrary> computed = new NearestLibrarySelector()
                    .selectTopN(uLat, uLon, nearby, apiResults, 5);
            List<Integer> libCodes = computed.stream()
                    .map(lib -> Integer.parseInt(lib.getLibCode())) // String → int 파싱
                    .toList();
//        List<Library> libs = repository.findByLibCodeIn(libCodes);
            log.info("TOP5: {}", computed);

            List<Library> libs =   checkLibCodes(libCodes);

            Map<String, Library> libMap = libs.stream()
                        .collect(Collectors.toMap(l ->
                                        String.valueOf(l.getLibCode()),
                                Function.identity(),
                                (a, b) -> a));

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

    private static List<Library> checkLibCodes(List<Integer> libCodes) {
        return libCodes.stream()
            .map(LibraryGeoService.cache::getIfPresent) // 캐시에서 있으면 꺼냄
            .filter(Objects::nonNull)
            .toList();
    }

    //        @Async("externalApiTaskExecutor")
        public CompletableFuture<List<AbstractMap.SimpleEntry<String, BookSearchReseponseDto>>>
        getCompletableFutureList(UserLocation location, List<String> libraryCodes) {

            List<CompletableFuture<AbstractMap.SimpleEntry<String, BookSearchReseponseDto>>> futures =
                    libraryCodes.stream()
                            .map(libCode -> CompletableFuture
                                    .supplyAsync(() -> callBookExist(libCode, location.getIsbn()), externalApiTaskExecutor)
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
        private AbstractMap.SimpleEntry<String, BookSearchReseponseDto> callBookExist(String libCode, String isbn){
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

}
