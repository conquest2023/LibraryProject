//package project.library.service;
//
//import lombok.RequiredArgsConstructor;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.data.geo.Point;
//import org.springframework.data.redis.core.HashOperations;
//import org.springframework.data.redis.core.RedisOperations;
//import org.springframework.data.redis.core.SessionCallback;
//import org.springframework.stereotype.Service;
//import project.library.controller.dto.UserLocation;
//import project.library.controller.dto.book.NearestLibrary;
//import project.library.controller.dto.book.search.BookSearchReseponseDto;
//import project.library.infrastructure.book.BookExistClient;
//import project.library.repository.NearestLibraryDetail;
//import project.library.repository.collection.Library;
//import project.library.service.domain.LibraryFinder;
//import project.library.service.domain.NearestLibrarySelector;
//
//import java.util.AbstractMap;
//import java.util.ArrayList;
//import java.util.List;
//import java.util.Map;
//import java.util.function.Function;
//import java.util.stream.Collectors;
//
//@Service
//@Slf4j
//@RequiredArgsConstructor
//public class TestService {
//
//
//    private final LibraryFinder finder;
//
//    private final BookExistClient client;
//
//
//    public List<NearestLibraryDetail> findTestNearbyLibrary(UserLocation userLocation) {
//
//
//        Map<String, Point> nearby = finder.findNearby(userLocation, 10);
//
//        List<String> libraryCodes = new ArrayList<>(nearby.keySet());
//
//        log.info("반경 내 도서관 {}개 발견.", libraryCodes.size());
//
//
//        // 2) 외부 API 병렬 호출 결과 수집
//        List<AbstractMap.SimpleEntry<String, BookSearchReseponseDto>> apiResults =
//                client.checkBookExistInParallel(userLocation, libraryCodes).join();
//
//        double uLat = userLocation.getLatitude();
//        double uLon = userLocation.getLongitude();
//
//        List<NearestLibrary> computed = new NearestLibrarySelector()
//                .selectTopN(uLat, uLon, nearby, apiResults, 5);
//        List<Integer> libCodes = computed.stream()
//                .map(lib -> Integer.parseInt(lib.getLibCode())) // String → int 파싱
//                .toList();
//
////            List<Library> libs = repository.findByLibCodeIn(libCodes);
//        log.info("TOP5: {}", computed);
//
//        List<Library> libs = checkRedisLibCodes(libCodes);
//        Map<String, Library> libMap = libs.stream()
//                .collect(Collectors.toMap(l ->
//                                String.valueOf(l.getLibCode()),
//                        Function.identity(),
//                        (a, b) -> a));
//
//        List<NearestLibraryDetail> result = new ArrayList<>();
//        for (NearestLibrary n : computed) {
//            Library lib = libMap.get(n.getLibCode());
//            if (lib != null) {
//                result.add(new NearestLibraryDetail(
//                        n.getLibCode(),
//                        lib.getLibName(),
//                        n.getIsLoan(),
//                        lib.getAddress(),
//                        lib.getTel(),
//                        lib.getLatitude(),
//                        lib.getLongitude(),
//                        n.getDistanceKm()
//                ));
//            }
//        }
//        return result;
//    }
//
//}
