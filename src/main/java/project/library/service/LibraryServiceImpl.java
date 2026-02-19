package project.library.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.geo.Point;
import org.springframework.data.redis.core.*;
import org.springframework.stereotype.Service;
import project.library.controller.dto.UserLocation;
import project.library.controller.dto.book.BookDto;
import project.library.controller.dto.book.NearestLibrary;
import project.library.controller.dto.book.search.BookSearchReseponseDto;
import project.library.infrastructure.SearchHistoryPort;
import project.library.infrastructure.book.BookExistClient;
import project.library.infrastructure.book.BookSearchClient;
import project.library.repository.NearestLibraryDetail;
import project.library.repository.collection.Library;
import project.library.service.domain.LibraryFinder;
import project.library.service.domain.LibraryGeoService;
import project.library.service.domain.NearestLibrarySelector;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class LibraryServiceImpl implements LibraryService {


    private final LibraryFinder finder;


    private final BookExistClient client;

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;


    private final SearchHistoryPort searchHistoryPort;

    private final BookSearchClient searchClient;

    @Override
    @CircuitBreaker(name = "bookSearchService", fallbackMethod = "fallbackNearbyLibrary")
    public List<NearestLibraryDetail> findNearbyLibrary(UserLocation userLocation) {


            Map<String, Point> nearby = finder.findNearby(userLocation, 10);

            List<String> libraryCodes = new ArrayList<>(nearby.keySet());

            log.info("반경 내 도서관 {}개 발견.", libraryCodes.size());


            // 2) 외부 API 병렬 호출 결과 수집
            List<AbstractMap.SimpleEntry<String, BookSearchReseponseDto>> apiResults =
                    client.checkBookExistInParallel(userLocation, libraryCodes).join();

            double uLat = userLocation.getLatitude();
            double uLon = userLocation.getLongitude();

            List<NearestLibrary> computed =
                     new NearestLibrarySelector()
                    .selectTopN(uLat, uLon, nearby, apiResults, 5);


            List<Integer> libCodes = computed.stream()
                    .map(lib -> Integer.parseInt(lib.getLibCode())) // String → int 파싱
                    .toList();

//            List<Library> libs = repository.findByLibCodeIn(libCodes);
            log.info("TOP5: {}", computed);

            List<Library> libs = checkLibCodes(libCodes);

            Map<String, Library> libMap = libs.stream()
                        .collect(Collectors.toMap(l ->
                                        String.valueOf(l.getLibCode()),
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


    @Override
    @CircuitBreaker(name = "bookSearchService", fallbackMethod = "fallbackSearchBook")
    public List<BookDto> searchBook(String sessionId, String title) {
        List<BookDto> searchBook = searchClient.searchBook(title);
        searchHistoryPort.addHistory(sessionId,title);
        return searchBook;
    }

    @Override
    public List<String> getHistoryBook(String sessionId) {
        return searchHistoryPort.getHistory(sessionId);
    }

    private static List<Library> checkLibCodes(List<Integer> libCodes) {
        Map<Integer, Library> results = LibraryGeoService.cache.getAllPresent(libCodes);

        List<Library> out = new ArrayList<>();
        for (Integer code : libCodes) {
            // 캐시에 없는 경우 null이 추가되지만, filter로 제거.
            out.add(results.get(code));
        }

        // 3. null 제거 후 반환 (선택 사항)
        return out.stream()
                .filter(Objects::nonNull)
                .toList();
    }

    public List<Library> checkRedisLibCodes(List<Integer> libCodes) {
        List<String> keys = libCodes.stream().map(c -> "library:" + c).toList();

        SessionCallback<Object> sc = new SessionCallback<>() {
            @Override
            @SuppressWarnings("unchecked")
            public Object execute(RedisOperations operations) {
                // ops를 String 키로 명시
                RedisOperations<String, ?> ops = (RedisOperations<String, ?>) operations;
                HashOperations<String, Object, Object> h = ops.opsForHash();
                for (String k : keys) {
                    h.entries(k); // <-- 여기서 더이상 K 미스매치 없음
                }
                return null;
            }
        };

        @SuppressWarnings("unchecked")
        List<Object> raws = (List<Object>) redisTemplate.executePipelined(sc);

        // raws[i] = i번째 key의 HGETALL 결과(Map<?,?>)
        List<Library> out = new ArrayList<>(raws.size());
        for (int i = 0; i < raws.size(); i++) {
            Object o = raws.get(i);
            if (!(o instanceof Map<?, ?> raw) || raw.isEmpty()) { out.add(null); continue; }

            // 키/값을 문자열로 통일
            Map<String, String> map = new HashMap<>();
            raw.forEach((k, v) -> map.put(String.valueOf(k), v == null ? null : String.valueOf(v)));

            out.add(new Library(libCodes.get(i),map.get("address"),map.get("libName"),map.get("closed"),Double.valueOf(map.get("latitude")),Double.valueOf(map.get("longitude")),map.get("tel"),map.get("homepage")));
        }

        // null 제거하고 반환(원하면 유지해도 됨)
        List<Library> result = out.stream()
                .filter(Objects::nonNull)
                .toList();

        log.info("Mapped Libraries: {}", result);
        return result;
    }

    public List<NearestLibraryDetail> fallbackNearbyLibrary(UserLocation userLocation, Throwable t) {
        log.error("도서관 조회 서비스 장애 발생! 사유: {}", t.getMessage());

        return List.of(new NearestLibraryDetail("현재 서비스가 지연 중입니다."));
    }

    public List<BookDto> fallbackSearchBook(String title,String ignoredParam, Throwable t) {
        log.error("책 검색 API 장애 발생. 검색어: {}, 원인: {}", title, t.getMessage());
        return List.of(BookDto.createError("검색 서비스가 일시적으로 지연되고 있습니다."));
    }
}
