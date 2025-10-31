package project.library.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.geo.Point;
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

    private final SearchHistoryPort searchHistoryPort;

    private final BookSearchClient searchClient;

    @Override
    public List<NearestLibraryDetail> findNearbyLibrary(UserLocation userLocation) {


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

//        List<Library> libs = repository.findByLibCodeIn(libCodes);
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



    //        @Async("externalApiTaskExecutor")

}
