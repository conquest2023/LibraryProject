package project.library.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.geo.Point;
import org.springframework.data.redis.core.*;
import org.springframework.data.redis.serializer.JdkSerializationRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializer;
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

import java.nio.charset.StandardCharsets;
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
    public List<NearestLibraryDetail> findNearbyLibrary(UserLocation userLocation) {


            Map<String, Point> nearby = finder.findNearby(userLocation, 10);

            List<String> libraryCodes = new ArrayList<>(nearby.keySet());

            log.info("반경 내 도서관 {}개 발견.", libraryCodes.size());


            // 2) 외부 API 병렬 호출 결과 수집
            List<AbstractMap.SimpleEntry<String, BookSearchReseponseDto>> apiResults =
                    client.checkBookExistInParallel(userLocation, libraryCodes).join();

            double uLat = userLocation.getLatitude();
            double uLon = userLocation.getLongitude();

            List<NearestLibrary> computed = new NearestLibrarySelector()
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

    @Override
    public List<NearestLibraryDetail> findTestNearbyLibrary(UserLocation userLocation) {


        Map<String, Point> nearby = finder.findNearby(userLocation, 3);

        List<String> libraryCodes = new ArrayList<>(nearby.keySet());

        log.info("반경 내 도서관 {}개 발견.", libraryCodes.size());


        // 2) 외부 API 병렬 호출 결과 수집
        List<AbstractMap.SimpleEntry<String, BookSearchReseponseDto>> apiResults =
                client.checkBookExistInParallel(userLocation, libraryCodes).join();

        double uLat = userLocation.getLatitude();
        double uLon = userLocation.getLongitude();

        List<NearestLibrary> computed = new NearestLibrarySelector()
                .selectTopN(uLat, uLon, nearby, apiResults, 5);
        List<Integer> libCodes = computed.stream()
                .map(lib -> Integer.parseInt(lib.getLibCode())) // String → int 파싱
                .toList();

//            List<Library> libs = repository.findByLibCodeIn(libCodes);
        log.info("TOP5: {}", computed);

        List<Library> libs = checkRedisLibCodes(libCodes);
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



    @Override
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
        return libCodes.stream()
            .map(LibraryGeoService.cache::getIfPresent) // 캐시에서 있으면 꺼냄
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

    /** 해시 직렬화기에 맞춰 byte[] → String 복원 (JDK/JSON/String 모두 대응) */
    private String safeDeserializeToString(RedisSerializer<Object> serializer, byte[] bytes) {
        if (bytes == null) return null;
        if (serializer == null) {
            // 혹시라도 serializer가 null이면 UTF-8로 시도
            return new String(bytes, StandardCharsets.UTF_8);
        }
        Object obj = serializer.deserialize(bytes);
        return (obj == null) ? null : obj.toString();
    }


}
