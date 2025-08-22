package project.library.service;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.geo.Circle;
import org.springframework.data.geo.Distance;
import org.springframework.data.geo.GeoResults;
import org.springframework.data.geo.Point;
import org.springframework.data.redis.connection.RedisGeoCommands;
import org.springframework.data.redis.core.GeoOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.domain.geo.GeoLocation;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;
import project.library.controller.dto.LocationDto;
import project.library.controller.dto.book.BookDetailDto;
import project.library.controller.dto.book.BookDto;
import project.library.controller.dto.book.check.BookCheck;
import project.library.controller.dto.book.search.BookSearchReseponseDto;
import project.library.repository.LibraryRepository;
import project.library.repository.collection.Library;

import java.net.URI;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class LibraryServiceImpl implements  LibraryService{


    private final RedisTemplate redisTemplate;

    private final RestTemplate restTemplate;

    private final LibraryRepository repository;

    private static final String API_BASE_URL = "http://data4library.kr/api";

    private static final String AUTH_KEY = "1df2f040d9555558e014f541e2908356008ca9e3aa7a1d9c43ec2c15e54f5f4b";




    public List<Library> findAll(){

         return  repository.findAll();
    }


    public List<Library> calculateDistance(LocationDto locationDto) {

        Point userLocation = new Point(locationDto.getLongitude(), locationDto.getLatitude());
        Distance radius = new Distance(10, RedisGeoCommands.DistanceUnit.KILOMETERS);
        Circle area = new Circle(userLocation, radius);

        GeoResults<GeoLocation<String>> results = redisTemplate.opsForGeo().radius("libraries:locations", area);
        List<String> libraryCodes = results.getContent().stream()
                .map(result -> result.getContent().getName())
                .collect(Collectors.toList());

        log.info("반경 내 도서관 {}개 발견.", libraryCodes.size());

        // CompletableFuture를 이용해 각 API 호출을 비동기적으로 실행
        List<CompletableFuture<BookSearchReseponseDto>> futures = libraryCodes.stream()
                .map(libCode -> CompletableFuture.supplyAsync(() -> {
                    try {
                        // RestTemplate을 사용한 API 호출 로직
                        URI uri = UriComponentsBuilder
                                .fromUriString(API_BASE_URL)
                                .pathSegment("bookExist")
                                .queryParam("authKey", AUTH_KEY)
                                .queryParam("libCode", libCode)
                                .queryParam("isbn13", locationDto.getIsbn())
                                .queryParam("format", "json")
                                .encode()
                                .build()
                                .toUri();

                        HttpHeaders headers = new HttpHeaders();
                        headers.setAccept(List.of(MediaType.ALL));
                        HttpEntity<String> entity = new HttpEntity<>(headers);

                        ResponseEntity<BookSearchReseponseDto> responseEntity =
                                restTemplate.exchange(uri, HttpMethod.GET, entity, BookSearchReseponseDto.class);

                        return responseEntity.getBody();

                    } catch (Exception e) {
                        log.error("API 호출 실패: {}", libCode, e);
                        return null;
                    }
                }))
                .collect(Collectors.toList());

        // 모든 비동기 작업이 완료될 때까지 기다림
        CompletableFuture<Void> allFutures = CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]));

        try {
            allFutures.join(); // 모든 Future가 끝날 때까지 블록킹

            // 완료된 결과들을 모아 처리
            List<BookSearchReseponseDto> responses = futures.stream()
                    .map(CompletableFuture::join)
                    .filter(response -> response != null && response.getResponse() != null)
                    .collect(Collectors.toList());
            // 최종 결과를 출력
            for (BookSearchReseponseDto searchResponse : responses) {
                BookCheck books = searchResponse.getResponse().getResult();
                if (books.getHasBook().equals("Y") && books.getLoanAvailable().equals("Y")) {
                    log.info("isbn:{}", locationDto.getIsbn());
                    log.info("책 제목: {}", books.getHasBook());
                    log.info("저자: {}", books.getLoanAvailable());
                }
            }
        } catch (Exception e) {
            log.error("비동기 작업 중 오류 발생.", e);
        }

        return null;
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
